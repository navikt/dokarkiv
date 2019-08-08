package no.nav.dokarkiv.core.security;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import com.auth0.jwt.JWT;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.jaxws.ThreadLocalSubjectHandler;
import no.nav.dokarkiv.core.security.ldap.NavLdapService;
import no.nav.dokarkiv.core.security.ldap.NavUser;
import no.nav.freg.security.oidc.auth.idtoken.extract.HeaderTokenExtractor;
import no.nav.modig.core.context.SubjectHandler;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Slf4j
@SuppressWarnings("Duplicates")
public class ValidateSakApiInterceptor implements HandlerInterceptor {

	private final NavLdapService navLdapService;
	private static final String UKJENT = "UKJENT";
	private final HeaderTokenExtractor headerTokenExtractor = new HeaderTokenExtractor();

	public ValidateSakApiInterceptor(NavLdapService navLdapService) {
		this.navLdapService = navLdapService;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		((ThreadLocalSubjectHandler) SubjectHandler.getSubjectHandler()).reset();

		if (response.getStatus() != HttpStatus.OK.value()) {
			//This means that the validation of oidc tokens failed in IdTokenAuthenticationFilter and we should let the handler go through
			return true;
		}

		//TODO SAML sjekk
		//TODO Basic auth
		putAbacMdcValues(request);

		String authorizationToken = headerTokenExtractor.getIdToken(request);

		String userName = getSubjectFromToken(authorizationToken);
		NavUser navUser = navLdapService.findByUserId(userName);
		NavUser navServiceUser = navLdapService.findByServiceuserId(userName);
		if (navUser.isUserExistsInLdap() || navServiceUser.isUserExistsInLdap()) {
			MDC.put(MDCConstants.MDC_CONSUMER_ID, userName);
			MDC.put(MDCConstants.MDC_USER_ID, userName);
			MDC.put(MDCConstants.MDC_USER_NAME, userName);
		} else {
			String message = "OIDC token på Authorization header må tilhøre en Servicebruker når Nav-Consumer-Token header ikke er satt";
			log.warn(message);
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
			return false;
		}

		return true;

	}

	private void putAbacMdcValues(HttpServletRequest request) {
		MDC.put(MDCConstants.MDC_HTTP_ENDPOINT, request.getRequestURL().toString());
		MDC.put(MDCConstants.MDC_HTTP_OPERATION, request.getMethod());
	}

	private String getSubjectFromToken(String token) {
		if (isEmpty(token)) {
			return null;
		}
		return JWT.decode(token).getSubject();
	}
}
