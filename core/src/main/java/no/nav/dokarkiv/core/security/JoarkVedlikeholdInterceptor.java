package no.nav.dokarkiv.core.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.consumer.azure.AzureAdGraphService;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;

import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static java.lang.String.format;
import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;

@Slf4j
public class JoarkVedlikeholdInterceptor implements HandlerInterceptor {

	private final HeaderTokenExtractor headerTokenExtractor = new HeaderTokenExtractor();
	private final AzureAdGraphService azureAdGraphService;
	private final String joarkVedlikeholdGroupObjectId;

	public JoarkVedlikeholdInterceptor(AzureAdGraphService azureAdGraphService, String joarkVedlikeholdGroupObjectId) {
		this.azureAdGraphService = azureAdGraphService;
		this.joarkVedlikeholdGroupObjectId = joarkVedlikeholdGroupObjectId;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		if (erIkkeEtOboToken()) {
			log.warn("OIDC-token på Authorization-header kan ikke være et system-til-system-token");

			response.sendError(SC_UNAUTHORIZED, "OIDC-token på Authorization-header må være et on behalf of-token");
			return false;
		}

		String authorizationToken = headerTokenExtractor.getIdToken(request);

		if (!isMemberOfGroupJoarkVedlikehold(authorizationToken, joarkVedlikeholdGroupObjectId)) {
			log.error(format("NAV-ansatt er ikke medlem av gruppen med objectId=\"%s\" i Entra ID", joarkVedlikeholdGroupObjectId));

			response.sendError(SC_UNAUTHORIZED, format("NAV-ansatt må være medlem av gruppen med objectId=\"%s\" i Entra ID", joarkVedlikeholdGroupObjectId));
			return false;
		}

		return true;
	}

	private boolean erIkkeEtOboToken() {
		return MDC.get(MDC_USER_ID).equals(MDC.get(MDC_CONSUMER_ID));
	}

	public boolean isMemberOfGroupJoarkVedlikehold(String token, String entraIdGroup) {
		DecodedJWT decode = JWT.decode(token);
		String userObjectId = decode.getClaim("oid").asString();

		return azureAdGraphService.isUserMemberOfGroup(userObjectId, entraIdGroup);
	}

}