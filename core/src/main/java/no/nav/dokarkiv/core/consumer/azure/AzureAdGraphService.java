package no.nav.dokarkiv.core.consumer.azure;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.OnBehalfOfCredential;
import com.azure.identity.OnBehalfOfCredentialBuilder;
import com.azure.identity.TokenCachePersistenceOptions;
import com.microsoft.graph.models.DirectoryObject;
import com.microsoft.graph.models.User;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import no.nav.dokarkiv.core.security.azure.AzureConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.List;

import static no.nav.dokarkiv.core.cache.CacheConfig.AZURE_CLIENT_CREDENTIAL_GRAPH_TOKEN_CACHE;
import static no.nav.dokarkiv.core.cache.CacheConfig.AZURE_ON_BEHALF_OF_TOKEN_CACHE;
import static no.nav.dokarkiv.core.cache.CacheConfig.NAVUSER_CACHE;

@Slf4j
@Component
@Profile({"nais", "local"})
public class AzureAdGraphService {

	private static final String BRUKER_IKKE_FUNNET = "Azure AD - Bruker ikke funnet";
	private final AzureConfig azureConfig;

	public AzureAdGraphService(AzureConfig azureConfig) {
		this.azureConfig = azureConfig;
	}

	@Cacheable(value = NAVUSER_CACHE, key = "#navIdent")
	@Retryable(exclude = DokarkivFunctionalException.class, maxAttempts = 5, backoff = @Backoff(delay = 200))
	public String hentFulltNavn(String navIdent) {
		User user = getUser(navIdent);

		if (user == null) {
			return null;
		}

		return user.getGivenName() + " " + user.getSurname();
	}

	@Retryable(exclude = DokarkivFunctionalException.class, maxAttempts = 5, backoff = @Backoff(delay = 200))
	public Boolean isUserMemberOfGroup(String userObjectId, String groupObjectId, String token) {
		log.info("Sjekk isUserMemberOfGroup for userObjectId={} på groupObjectId={}", userObjectId, groupObjectId);
		List<String> groups = getGroupsForUserObjectId(userObjectId, token);

		var containsCorrectGroup = groups.contains(groupObjectId);
		log.info("Inni isUserMemberOfGroup der containsCorrectGroup={}", containsCorrectGroup);

		return containsCorrectGroup;
	}

	private User getUser(String navIdent) {
		List<User> users = clientCredentialgetGraphClient().users()
				.get(requestConfig -> {
					requestConfig.headers.add("ConsistencyLevel", "eventual");
					requestConfig.queryParameters.filter = "onPremisesSamAccountName eq '" + navIdent + "'";
					requestConfig.queryParameters.count = true;
					requestConfig.queryParameters.select = new String[]{"givenname", "surname"};
				}).getValue();

		if (users.size() != 1) {
			log.warn("Azure AD finner ikke bruker med ident={}. {}", navIdent, BRUKER_IKKE_FUNNET);
			return null;
		}
		return users.get(0);
	}

	private List<String> getGroupsForUserObjectId(String userObjectId, String token) {
		List<DirectoryObject> result = onBehalfOfTokenGraphServiceClient(token)
				.users()
				.byUserId(userObjectId)
				.memberOf()
				.get().getValue();

		return result.stream()
				.map(group -> group.getId())
				.toList();
	}

	GraphServiceClient clientCredentialgetGraphClient() {
		TokenCachePersistenceOptions tokenCachePersistenceOptions = new TokenCachePersistenceOptions()
				.setName(AZURE_CLIENT_CREDENTIAL_GRAPH_TOKEN_CACHE);
		ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
				.tenantId(azureConfig.getAppTenant())
				.clientId(azureConfig.getAppClientId())
				.clientSecret(azureConfig.getAppClientSecret())
				.tokenCachePersistenceOptions(tokenCachePersistenceOptions)
				.build();
		return new GraphServiceClient(clientSecretCredential);
	}

	GraphServiceClient onBehalfOfTokenGraphServiceClient(String accessToken) {
		TokenCachePersistenceOptions tokenCachePersistenceOptions = new TokenCachePersistenceOptions()
				.setName(AZURE_ON_BEHALF_OF_TOKEN_CACHE);
		OnBehalfOfCredential onBehalfOfCredential = new OnBehalfOfCredentialBuilder()
				.userAssertion(accessToken)
				.clientId(azureConfig.getAppClientId())
				.clientSecret(azureConfig.getAppClientSecret())
				.tenantId(azureConfig.getAppTenant())
				.tokenCachePersistenceOptions(tokenCachePersistenceOptions)
				.build();
		return new GraphServiceClient(onBehalfOfCredential);
	}
}