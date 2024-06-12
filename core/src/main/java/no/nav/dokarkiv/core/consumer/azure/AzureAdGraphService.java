package no.nav.dokarkiv.core.consumer.azure;

import com.microsoft.graph.models.DirectoryObject;
import com.microsoft.graph.models.User;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.List;

import static no.nav.dokarkiv.core.cache.CacheConfig.NAVUSER_CACHE;

@Slf4j
@Component
@Profile({"nais", "local"})
public class AzureAdGraphService {

	private static final String BRUKER_IKKE_FUNNET = "Azure AD - Bruker ikke funnet";
	private final GraphServiceClient graphServiceClient;

	public AzureAdGraphService(GraphServiceAuthenticationProvider dokarkivAuthenticationProvider) {
		this.graphServiceClient = new GraphServiceClient(dokarkivAuthenticationProvider);
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
		List<User> users = graphServiceClient.users()
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
		List<DirectoryObject> result = graphServiceClient
				.users()
				.byUserId(userObjectId)
				.memberOf()
				.get().getValue();

		return result.stream()
				.map(group -> group.getId())
				.toList();
	}
}