package no.nav.dokarkiv.core.mdc;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.security.ldap.NavLdapService;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Slf4j

public class MDCInHandlerRest implements HandlerInterceptor {

	private final NavLdapService navLdapService;

	public MDCInHandlerRest(NavLdapService navLdapService) {
		this.navLdapService = navLdapService;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		List<String> headers = getOidcAuthHeaders(request.getHeaders("Authorization"));

		if (headers.isEmpty()) {
			log.warn("Kunne ikke autorisere forespoersel. Finner ingen header med key=Authorization og value=Bearer *oidcToken*.");
			return true;
		} else {
			headers.forEach(header -> {
				DecodedJWT decodedJWT = JWT.decode(header.substring(7));
				String subject = decodedJWT.getSubject();

				if (isServiceUser(subject)) {
					MDC.put(MDCConstants.MDC_CONSUMER_ID, subject);
				} else if (isUser(subject)) {
					MDC.put(MDCConstants.MDC_USER_ID, subject);
				}
			});

			if (isEmpty(MDC.get(MDCConstants.MDC_CONSUMER_ID))) {
				MDC.put(MDCConstants.MDC_CONSUMER_ID, MDC.get(MDCConstants.MDC_USER_ID));
			} else if (isEmpty(MDC.get(MDCConstants.MDC_USER_ID))) {
				MDC.put(MDCConstants.MDC_USER_ID, MDC.get(MDCConstants.MDC_CONSUMER_ID));
			}

			return true;
		}
	}

	private boolean isServiceUser(String subjectId) {
		return navLdapService.findByServiceuserId(subjectId).exists();
	}

	private boolean isUser(String subjectId) {
		return navLdapService.findByUserId(subjectId).exists();
	}

	private List<String> getOidcAuthHeaders(Enumeration<String> headers) {
		List<String> returnObject = new ArrayList<>();
		while (headers.hasMoreElements()) {
			String header = headers.nextElement();
			if (header.startsWith("Bearer ")) {
				returnObject.add(header);
			}
		}
		return returnObject;
	}
}
