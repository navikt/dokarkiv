package no.nav.dokarkiv.core.security;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.NavHeaders;
import no.nav.dokarkiv.core.util.DecodeUtils;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Slf4j
public class PopulateMDCHandler implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		populateCallId(request);
		populateConsumerId(request);
		populateUserId(request);
		return true;
	}

	private void populateUserId(HttpServletRequest request) {
		final String navUserId = request.getHeader(NavHeaders.NAV_USER_ID);
		if (isNotBlank(navUserId)) {
			MDC.put(MDCConstants.MDC_USER_ID, navUserId);
		}
	}

	private void populateCallId(HttpServletRequest request) {
		final String navCallId = request.getHeader(NavHeaders.NAV_CALL_ID);
		if (isNotBlank(navCallId)) {
			MDC.put(MDCConstants.MDC_CALL_ID, navCallId);
			return;
		}

		final String xCorrelationId = request.getHeader(NavHeaders.X_CORRELATION_ID);
		if (isNotBlank(xCorrelationId)) {
			MDC.put(MDCConstants.MDC_CALL_ID, xCorrelationId);
			return;
		}

		final String callIdHeader = request.getHeader("callId");
		if (isNotBlank(callIdHeader)) {
			MDC.put(MDCConstants.MDC_CALL_ID, callIdHeader);
			return;
		}
		// Fallback
		MDC.put(MDCConstants.MDC_CALL_ID, UUID.randomUUID().toString());
	}

	private void populateConsumerId(HttpServletRequest request) {
		String navConsumerId = request.getHeader(NavHeaders.NAV_CONSUMER_ID);

		String usernameBasicAuth = getUsernameFromBasicAuth(request);
		if(isNotBlank(usernameBasicAuth)) {
			navConsumerId = usernameBasicAuth;
		}

		if (isNotBlank(navConsumerId)) {
			MDC.put(MDCConstants.MDC_CONSUMER_ID, navConsumerId);
		}
	}

	private String getUsernameFromBasicAuth(HttpServletRequest request) {
		String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (isNotBlank(authorizationHeader) && authorizationHeader.startsWith("Basic")) {
			try {
				String[] strings = DecodeUtils.decodeBasicAuth(authorizationHeader);
				return strings[0];
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}
}