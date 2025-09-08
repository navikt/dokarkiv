package no.nav.dokarkiv.core.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

import static jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static java.lang.String.format;
import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
public class JoarkVedlikeholdTokenClaimOnlyInterceptor implements HandlerInterceptor {

	private final HeaderTokenExtractor headerTokenExtractor = new HeaderTokenExtractor();
	private final String joarkVedlikeholdGroupObjectId;

	public JoarkVedlikeholdTokenClaimOnlyInterceptor(String joarkVedlikeholdGroupObjectId) {
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
			log.error(format("NAV-ansatt har ikke gruppen med objectId=\"%s\" i Entra ID token claims", joarkVedlikeholdGroupObjectId));

			response.sendError(SC_FORBIDDEN, format("NAV-ansatt må ha gruppen med objectId=\"%s\" i Entra ID token claims", joarkVedlikeholdGroupObjectId));
			return false;
		}

		return true;
	}

	private boolean erIkkeEtOboToken() {
		return MDC.get(MDC_USER_ID).equals(MDC.get(MDC_CONSUMER_ID));
	}

	public boolean isMemberOfGroupJoarkVedlikehold(String token, String entraIdGroup) {
		DecodedJWT decode = JWT.decode(token);
		List<String> groupsInClaim = decode.getClaim("groups").asList(String.class);

		return !groupsInClaim.isEmpty() && groupsInClaim.contains(entraIdGroup);
	}

}