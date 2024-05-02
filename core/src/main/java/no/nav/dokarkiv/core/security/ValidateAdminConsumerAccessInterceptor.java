package no.nav.dokarkiv.core.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.consumer.azure.AzureAdGraphService;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

import static java.lang.String.format;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;

@Slf4j
public class ValidateAdminConsumerAccessInterceptor implements HandlerInterceptor {

	private static final Set<String> VALID_AZURE_OBO_CALLERS = Set.of("joarkadmin");
	private static final Set<String> VALID_STS_CALLERS = Set.of("srvjoarkadmin");

	private final HeaderTokenExtractor headerTokenExtractor = new HeaderTokenExtractor();
	private final AzureAdGraphService azureAdGraphService;
	private final String joarkVedlikeholdGroupObjectId;

	public ValidateAdminConsumerAccessInterceptor(AzureAdGraphService azureAdGraphService, String joarkVedlikeholdGroupObjectId) {
		this.azureAdGraphService = azureAdGraphService;
		this.joarkVedlikeholdGroupObjectId = joarkVedlikeholdGroupObjectId;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		String authorizationToken = headerTokenExtractor.getIdToken(request);

		// Som hovedregel skal validatoren sjekke on behalf of-token
		// Unntak for dette er automatiske jobber på joarkadmin som kaller admin-endepunkt med STS-token
		if (MDC.get(MDC_USER_ID).equals(MDC.get(MDC_CONSUMER_ID))) {
			if (VALID_STS_CALLERS.contains(MDC.get(MDC_CONSUMER_ID))) {
				return true;
			} else {
				log.warn("OIDC-token på Authorization-header kan ikke være et client credential-token");

				response.sendError(SC_UNAUTHORIZED, "OIDC-token på Authorization-header må være et on behalf of-token");
				return false;
			}
		}

		if (!consumerAppIsAllowed()) {
			log.warn(format("OIDC-token på Authorization-header tilhører ikke en av følgende apper=%s", VALID_AZURE_OBO_CALLERS));

			response.sendError(SC_UNAUTHORIZED, format("OIDC-token på Authorization-header må tilhøre en av følgende apper=%s", VALID_AZURE_OBO_CALLERS));
			return false;
		}

		if (!isMemberOfGroupJoarkVedlikehold(authorizationToken, joarkVedlikeholdGroupObjectId)) {
			log.error(format("NAV-ansatt er ikke medlem av gruppen med objectId=\"%s\" i Entra ID", joarkVedlikeholdGroupObjectId));

			response.sendError(SC_UNAUTHORIZED, format("NAV-ansatt må være medlem av gruppen med objectId=\"%s\" i Entra ID", joarkVedlikeholdGroupObjectId));
			return false;
		}

		return true;
	}

	private static boolean consumerAppIsAllowed() {
		final String appWithNamespace = MDC.get(MDC_CONSUMER_ID); // formatted as teamdokumenthandtering:joarkadmin

		if (appWithNamespace == null) {
			return false;
		}

		return VALID_AZURE_OBO_CALLERS.stream()
				.anyMatch(appWithNamespace::contains);
	}

	public boolean isMemberOfGroupJoarkVedlikehold(String token, String entraIdGroup) {
		DecodedJWT decode = JWT.decode(token);
		String userObjectId = decode.getClaim("oid").asString();

		return azureAdGraphService.isUserMemberOfGroup(userObjectId, entraIdGroup);
	}

}