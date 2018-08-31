package no.nav.dokarkiv.core.mdc;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import com.auth0.jwt.JWT;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.security.ldap.NavLdapService;
import no.nav.dokarkiv.core.security.ldap.NavUser;
import no.nav.freg.security.oidc.auth.idtoken.extract.HeaderTokenExtractor;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Slf4j

public class ValidateUserAndAddToMDCHandler implements HandlerInterceptor {

	private final NavLdapService navLdapService;

	private final HeaderTokenExtractor headerTokenExtractor = new HeaderTokenExtractor();

	public ValidateUserAndAddToMDCHandler(NavLdapService navLdapService) {
		this.navLdapService = navLdapService;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		if (response.getStatus() != HttpStatus.OK.value()) {
			//This means that the validation of oidc tokens failed in IdTokenAuthenticationFilter and we should let the handler go through
			return true;
		}

		String consumerToken = headerTokenExtractor.getConsumerToken(request);
		String userToken = headerTokenExtractor.getIdToken(request);

		if (isEmpty(consumerToken) || isEmpty(userToken)) {
			String message = "Finner ingen header med bruker eller consumer oidc-token. Requesten må ha oidc-token for bruker header key=Authorization og value=Bearer [oidc-token] og oidc-token for consumer i header med key=Nav-Consumer-Token og value=Bearer [oidc-token]";
			log.warn(message);
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
			return false;
		} else {

			String userId = getSubjectFromToken(userToken);
			NavUser user = navLdapService.findByUserId(userId);
			if (user.isExists()) {
				MDC.put(MDCConstants.MDC_USER_ID, userId);
				MDC.put(MDCConstants.MDC_USER_NAME, user.getFullname());
			} else {
				String message = format("Fant ikke bruker med id=%s i LDAP", user.getFullname());
				log.warn(message);
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
				return false;
			}

			String consumerID = getSubjectFromToken(consumerToken);
			NavUser consumer = navLdapService.findByServiceuserId(consumerID);
			if (consumer.isExists()) {
				MDC.put(MDCConstants.MDC_CONSUMER_ID, consumerID);
			} else {
				String message = format("Fant ikke servicebruker med id=%s i LDAP", consumer.getFullname());
				log.warn(message);
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
				return false;
			}

			return true;
		}
	}

	private String getSubjectFromToken(String token) {
		if (isEmpty(token)) {
			return null;
		}
		return JWT.decode(token).getSubject();
	}
}
