package no.nav.dokarkiv.core.consumer.azure;

import com.microsoft.graph.models.User;
import com.microsoft.graph.options.HeaderOption;
import com.microsoft.graph.options.Option;
import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.GraphServiceClient;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.consumer.azure.AzureGroupResponse.AzureGroup;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import no.nav.dokarkiv.core.exceptions.DokarkivTechnicalException;
import okhttp3.Request;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static no.nav.dokarkiv.core.cache.CacheConfig.AZURE_HENT_AD_GRUPPER;
import static no.nav.dokarkiv.core.cache.CacheConfig.NAVUSER_CACHE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
@Slf4j
@Profile({"nais", "local"})
public class AzureAdGraphService {

	private static final String BRUKER_IKKE_FUNNET = "Azure AD - Bruker ikke funnet";
	private static final String MICROSOFT_GRAPH_SCOPE_V2 = "https://graph.microsoft.com/";
	private static final String MICROSOFT_GRAPH_SCOPE_APP = MICROSOFT_GRAPH_SCOPE_V2 + ".default";

	private final AzureToken azureToken;
	private final RestTemplate restTemplate;

	public AzureAdGraphService(AzureToken azureToken, RestTemplate restTemplate) {
		this.azureToken = azureToken;
		this.restTemplate = restTemplate;
	}

	@Cacheable(value = NAVUSER_CACHE, key = "#navIdent")
	@Retryable(exclude = DokarkivFunctionalException.class, maxAttempts = 5, backoff = @Backoff(delay = 200))
	public String hentFulltNavn(String navIdent) {
		User user = getUser(navIdent);

		if (user == null) {
			return null;
		}

		return user.givenName + " " + user.surname;
	}

	@Cacheable(value = AZURE_HENT_AD_GRUPPER, key = "#navIdent")
	@Retryable(exclude = DokarkivFunctionalException.class, maxAttempts = 5, backoff = @Backoff(delay = 200))
	public Boolean userInGroup(String navIdent, String adGroup) {
		User user = getUser(navIdent);

		if (user == null) {
			return false;
		}

		List<String> groups = getAzureGroupsByPrincipal(user.id);
		return groups.contains(adGroup);
	}

	private String getUserToken() {
		return azureToken.clientCredentialAccessToken(MICROSOFT_GRAPH_SCOPE_APP);
	}

	GraphServiceClient<Request> getGraphClient(String accessToken) {
		return GraphServiceClient.builder()
				.authenticationProvider(url -> CompletableFuture.completedFuture(accessToken))
				.buildClient();
	}

	private User getUser(String navIdent) {
		LinkedList<Option> requestOptions = new LinkedList<Option>();
		requestOptions.add(new HeaderOption("ConsistencyLevel", "eventual"));
		requestOptions.add(new QueryOption("$filter", "onPremisesSamAccountName eq '" + navIdent + "'"));

		List<User> res = getGraphClient(getUserToken())
				.users()
				.buildRequest(requestOptions)
				.count(true)
				.select("givenname, surname")
				.get().getCurrentPage();

		if (res.size() != 1) {
			log.warn(format("Azure AD finner ikke bruker med ident=%s. %s", navIdent, BRUKER_IKKE_FUNNET));
			return null;
		}

		return res.get(0);
	}

	private List<String> getAzureGroupsByPrincipal(String userPrincipal) {
		try {
			String url = MICROSOFT_GRAPH_SCOPE_V2 + format("v1.0/users/%s/memberOf", userPrincipal);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(APPLICATION_JSON);
			headers.setBearerAuth(getUserToken());
			headers.setAccept(singletonList(APPLICATION_JSON));
			HttpEntity<String> requestEntity = new HttpEntity<>(headers);

			ResponseEntity<AzureGroupResponse> response = restTemplate.exchange(url, POST, requestEntity, AzureGroupResponse.class);

			List<AzureGroup> azureGroups = response.getBody().value();
			return azureGroups.stream().map(AzureGroup::id).toList();
		} catch (Exception e) {
			throw new DokarkivTechnicalException(format("Kunne ikke hente gruppeinformasjon fra Azure, Feilmelding=%s", e.getMessage()));
		}
	}

}