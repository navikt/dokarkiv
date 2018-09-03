package no.nav.dokarkiv.core.mdc;

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

		String consumerToken = headerTokenExtractor.getConsumerToken(request);
		String userToken = headerTokenExtractor.getIdToken(request);

		if (isEmpty(userToken)) {
			String message = "Finner ingen oidc token på Authentication header. Requesten må enten ha oidc-token for servicebruker på header med key=Authorization og value=Bearer [oidc-token] eller ha oidc-token for internbruker i Authentication header og servicebruker på header med key=Nav-Consumer-Token og value=Bearer [oidc-token]";
			log.warn(message);
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
			return false;
		} else {

		    if (isNotEmpty(userToken) && isNotEmpty(consumerToken)) {

                String userId = getSubjectFromToken(userToken);
                NavUser user = navLdapService.findByUserId(userId);
                if (user.isExists()) {
                    MDC.put(MDCConstants.MDC_USER_ID, userId);
                    MDC.put(MDCConstants.MDC_USER_NAME, user.getFullname());
                } else {
                    String message = "OIDC token på Authentication header må tilhøre en Internbruker når både Authentication og Nav-Consumer-Token header er satt";
                    log.warn(message);
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
                    return false;
                }

                String consumerID = getSubjectFromToken(consumerToken);
                NavUser consumer = navLdapService.findByServiceuserId(consumerID);
                if (consumer.isExists()) {
                    MDC.put(MDCConstants.MDC_CONSUMER_ID, consumerID);
                } else {
                    String message = "OIDC token på Authentication header må tilhøre en Servicebruker når Nav-Consumer-Token header er satt";
                    log.warn(message);
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
                    return false;
                }

            } else {

                String consumerID = getSubjectFromToken(consumerToken);
                NavUser consumer = navLdapService.findByServiceuserId(consumerID);
                if (consumer.isExists()) {
                    MDC.put(MDCConstants.MDC_CONSUMER_ID, consumerID);
                    MDC.put(MDCConstants.MDC_USER_ID, consumerID);
                    MDC.put(MDCConstants.MDC_USER_NAME, consumerID);
                } else {
                    String message = "OIDC token på Authentication header må tilhøre en Servicebruker når Nav-Consumer-Token header ikke er satt";
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
