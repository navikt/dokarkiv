package no.nav.dokarkiv.core.consumer.azure;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.models.DirectoryObject;
import com.microsoft.graph.models.Entity;
import com.microsoft.graph.models.User;
import com.microsoft.graph.models.odataerrors.MainError;
import com.microsoft.graph.models.odataerrors.ODataError;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import no.nav.dokarkiv.core.properties.DokarkivProperties;
import no.nav.dokarkiv.core.security.azure.AzureConfig;
import no.nav.dokarkiv.core.util.SafeLoggingUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.List;

import static no.nav.dokarkiv.core.cache.CacheConfig.NAVUSER_CACHE;

@Slf4j
@Component
public class AzureAdGraphService {

	private static final String BRUKER_IKKE_FUNNET = "Azure AD - Bruker ikke funnet";
	private final GraphServiceClient graphServiceClient;

	public AzureAdGraphService(DokarkivProperties dokarkivProperties,
							   AzureConfig azureConfig) {
		TokenCredential tokenCredential = new ClientSecretCredentialBuilder()
				.tenantId(azureConfig.getAppTenantId())
				.clientSecret(azureConfig.getAppClientSecret())
				.clientId(azureConfig.getAppClientId())
				.build();
		this.graphServiceClient = new GraphServiceClient(tokenCredential);
		String overrideMsGraphService = dokarkivProperties.getEndpoints().getOverrideMsGraphServiceRoot();
		if (StringUtils.isNotBlank(overrideMsGraphService)) {
			graphServiceClient.getRequestAdapter().setBaseUrl(overrideMsGraphService);
		}
	}

	@Cacheable(value = NAVUSER_CACHE, key = "#navIdent")
	@Retryable(excludes = DokarkivFunctionalException.class)
	public String hentFulltNavn(String navIdent) {
		User user = getUser(navIdent);

		if (user == null) {
			return null;
		}

		return user.getGivenName() + " " + user.getSurname();
	}

	@Retryable(excludes = DokarkivFunctionalException.class)
	public Boolean isUserMemberOfGroup(String userObjectId, String groupObjectId) {
		try {
			List<DirectoryObject> result = graphServiceClient
					.users()
					.byUserId(userObjectId)
					.memberOf()
					.get(requestConfig -> {
						requestConfig.queryParameters.filter = "id eq '" + groupObjectId + "'";
					})
					.getValue();

			return result != null && result.stream()
					.map(Entity::getId)
					.anyMatch(groupObjectId::equalsIgnoreCase);
		} catch (ODataError e) {
			MainError mainError = e.getError();
			log.error("Auth-feil mot msgraph: {} ; target: {} ; details: {}", mainError.getMessage(), mainError.getTarget(), mainError.getDetails(), e);
			throw e;
		}
	}

	private User getUser(String navIdent) {
		try {
			List<User> users = graphServiceClient.users()
					.get(requestConfig -> {
						requestConfig.headers.add("ConsistencyLevel", "eventual");
						requestConfig.queryParameters.filter = "onPremisesSamAccountName eq '" + navIdent + "'";
						requestConfig.queryParameters.count = true;
						requestConfig.queryParameters.select = new String[]{"givenname", "surname"};
					}).getValue();

			if (users.size() != 1) {
				log.warn("Azure AD finner ikke bruker med ident={}. {}", SafeLoggingUtil.removeUnsafeChars(navIdent), BRUKER_IKKE_FUNNET);
				return null;
			}
			return users.get(0);
		} catch (ODataError e) {
			MainError mainError = e.getError();
			log.error("Auth-feil mot msgraph: {} ; target: {} ; details: {}", mainError.getMessage(), mainError.getTarget(), mainError.getDetails(), e);
			throw e;
		}
	}
}
