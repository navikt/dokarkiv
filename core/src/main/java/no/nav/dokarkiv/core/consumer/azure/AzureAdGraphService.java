package no.nav.dokarkiv.core.consumer.azure;

import com.microsoft.graph.models.DirectoryObject;
import com.microsoft.graph.models.User;
import com.microsoft.graph.options.HeaderOption;
import com.microsoft.graph.options.Option;
import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.GraphServiceClient;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import okhttp3.Request;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static no.nav.dokarkiv.core.cache.CacheConfig.NAVUSER_CACHE;

@Slf4j
@Component
@Profile({"nais", "local"})
public class AzureAdGraphService {

	private static final String BRUKER_IKKE_FUNNET = "Azure AD - Bruker ikke funnet";
	private static final String MICROSOFT_GRAPH_SCOPE = "https://graph.microsoft.com/.default";

	private final AzureToken azureToken;

	public AzureAdGraphService(AzureToken azureToken) {
		this.azureToken = azureToken;
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

	@Retryable(exclude = DokarkivFunctionalException.class, maxAttempts = 5, backoff = @Backoff(delay = 200))
	public Boolean isUserMemberOfGroup(String userObjectId, String groupObjectId, String token, String subClaim) {
		List<String> groups = getGroupsForUserObjectId(userObjectId, token, subClaim);

		return groups.contains(groupObjectId);
	}

	private User getUser(String navIdent) {
		LinkedList<Option> requestOptions = new LinkedList<>();
		requestOptions.add(new HeaderOption("ConsistencyLevel", "eventual"));
		requestOptions.add(new QueryOption("$filter", "onPremisesSamAccountName eq '" + navIdent + "'"));

		List<User> res = getGraphClient(azureToken.clientCredentialAccessToken(MICROSOFT_GRAPH_SCOPE))
				.users()
				.buildRequest(requestOptions)
				.count(true)
				.select("givenname, surname")
				.get().getCurrentPage();

		if (res.size() != 1) {
			log.warn("Azure AD finner ikke bruker med ident={}. {}", navIdent, BRUKER_IKKE_FUNNET);
			return null;
		}

		return res.get(0);
	}

	private List<String> getGroupsForUserObjectId(String userObjectId, String token, String subClaim) {
		String onBehalfOfOrClientCredentialToken = azureToken.getAndCacheAzureOnBehalfOfAndClientCredentialToken(token, MICROSOFT_GRAPH_SCOPE, subClaim);

		List<DirectoryObject> result = getGraphClient(onBehalfOfOrClientCredentialToken)
				.users()
				.byId(userObjectId)
				.memberOf()
				.buildRequest()
				.get().getCurrentPage();

		return result.stream()
				.map(group -> group.id)
				.toList();
	}

	GraphServiceClient<Request> getGraphClient(String accessToken) {
		return GraphServiceClient.builder()
				.authenticationProvider(url -> CompletableFuture.completedFuture(accessToken))
				.buildClient();
	}

}