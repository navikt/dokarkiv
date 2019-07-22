package no.nav.dokarkiv.core.security;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.NavHeaders;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Slf4j
public class PopulateMDCHandler implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		populateCallId(request);
		populateConsumerId(request);

		MDC.put(MDCConstants.MDC_HTTP_ENDPOINT, request.getRequestURL().toString());
		MDC.put(MDCConstants.MDC_HTTP_OPERATION, request.getMethod());
		return true;
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
		final String navConsumerId = request.getHeader(NavHeaders.NAV_CONSUMER_ID);
		if (isNotBlank(navConsumerId)) {
			MDC.put(MDCConstants.MDC_CONSUMER_ID, navConsumerId);
		}
	}
}