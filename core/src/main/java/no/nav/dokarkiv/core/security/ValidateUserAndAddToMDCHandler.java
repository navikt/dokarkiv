package no.nav.dokarkiv.core.security;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

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

		String navConsumerToken = headerTokenExtractor.getConsumerToken(request);
		String authorizationToken = headerTokenExtractor.getIdToken(request);

		if (isEmpty(authorizationToken)) {
			String message = "Finner ingen oidc token på Authorization header. Requesten må enten ha oidc-token for servicebruker på header med key=Authorization og value=Bearer [oidc-token] eller ha oidc-token for internbruker i Authorization header og servicebruker på header med key=Nav-Consumer-Token og value=Bearer [oidc-token]";
			log.warn(message);
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
			return false;
		} else {
			if (isNotEmpty(authorizationToken) && isNotEmpty(navConsumerToken)) {
				//TODO: Kan dette også være en servicebruker?
				String userId = getSubjectFromToken(authorizationToken);
				NavUser navUser = navLdapService.findByUserId(userId);
				if (navUser.isUserExistsInLdap()) {
					MDC.put(MDCConstants.MDC_USER_ID, userId);
					MDC.put(MDCConstants.MDC_USER_NAME, navUser.getFullname());
				} else {
					String message = "OIDC token på Authorization header må tilhøre en Internbruker når både Authorization og Nav-Consumer-Token header er satt";
					log.warn(message);
					response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
					return false;
				}

				String consumerID = getSubjectFromToken(navConsumerToken);
				NavUser consumer = navLdapService.findByServiceuserId(consumerID);
				if (consumer.isUserExistsInLdap()) {
					MDC.put(MDCConstants.MDC_CONSUMER_ID, consumerID);
				} else {
					String message = "OIDC token på Nav-Consumer-Token header må tilhøre en Servicebruker når både Authorization og Nav-Consumer-Token header er satt";
					log.warn(message);
					response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
					return false;
				}
			} else {
				String consumerID = getSubjectFromToken(authorizationToken);
				NavUser consumer = navLdapService.findByServiceuserId(consumerID);
				if (consumer.isUserExistsInLdap()) {
					MDC.put(MDCConstants.MDC_CONSUMER_ID, consumerID);
					MDC.put(MDCConstants.MDC_USER_ID, consumerID);
					MDC.put(MDCConstants.MDC_USER_NAME, consumerID);
				} else {
					String message = "OIDC token på Authorization header må tilhøre en Servicebruker når Nav-Consumer-Token header ikke er satt";
					log.warn(message);
					response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
					return false;
				}
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
