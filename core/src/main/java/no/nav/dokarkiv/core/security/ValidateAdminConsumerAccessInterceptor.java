package no.nav.dokarkiv.core.security;

import com.auth0.jwt.JWT;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.security.ldap.NavLdapService;
import no.nav.dokarkiv.core.security.ldap.NavUser;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

import static org.apache.commons.lang3.BooleanUtils.isFalse;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * @author Ketill Fenne, Visma Consulting AS
 */
@Slf4j
public class ValidateAdminConsumerAccessInterceptor implements HandlerInterceptor {

	private final HeaderTokenExtractor headerTokenExtractor = new HeaderTokenExtractor();
	private final NavLdapService navLdapService;

	private static final String ADMIN_SERVICE_USER = "srvjoarkadmin";
	private static final String ADMIN_SERVICE_USER_AD_ROLE = "0000-GA-joark-vedlikehold";

	public ValidateAdminConsumerAccessInterceptor(NavLdapService navLdapService) {
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

		if (isEmpty(navConsumerToken)) {
			if (isFalse(isTokenBelongsToUser(authorizationToken, ADMIN_SERVICE_USER))) {
				String message = String.format("OIDC token på Authorization-header må tilhøre servicebruker på %s", ADMIN_SERVICE_USER);
				log.warn(message);
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
				return false;
			}
		} else if (isNotEmpty(navConsumerToken)) {
			if (isFalse(isTokenBelongsToUser(navConsumerToken, ADMIN_SERVICE_USER))) {
				String message = String.format("OIDC token på Nav-Consumer-Token header må tilhøre serviceuser på %s", ADMIN_SERVICE_USER);
				log.warn(message);
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
				return false;
			} else if (isFalse(isUserInTokenHasRole(authorizationToken, ADMIN_SERVICE_USER_AD_ROLE))) {
				String message = String.format("Bruker må være medlem av gruppen \"%s\"", ADMIN_SERVICE_USER_AD_ROLE);
				log.warn(message);
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
				return false;
			}
		} else {
			String message = "Token header må være satt";
			log.warn(message);
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
			return false;
		}
		return true;
	}

	public boolean isUserInTokenHasRole(String token, String ldapGroup) {
		String userId = getSubjectFromToken(token);
		NavUser user = navLdapService.findByUserId(userId);
		return user.isUserExistsInLdap() && contains(user.getMemberOf(), ldapGroup);
	}

	private boolean contains(Set<String> l, String s) {
		return l.stream().anyMatch(x -> x.contains(s));
	}

	public boolean isTokenBelongsToUser(String token, String subject) {
		if (isNotEmpty(token)) {
			String consumerID = getSubjectFromToken(token);
			return subject.equalsIgnoreCase(consumerID);
		}
		return false;
	}

	private String getSubjectFromToken(String token) {
		if (isEmpty(token)) {
			return null;
		}
		return JWT.decode(token).getSubject();
	}


}
