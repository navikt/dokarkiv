package no.nav.dokarkiv.core.security;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Slf4j
public class PopulateMDCHandler implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		if (request.getHeader("X-Correlation-ID") != null) {
			MDC.put(MDCConstants.MDC_CALL_ID, request.getHeader("X-Correlation-ID"));
		}

		MDC.put(MDCConstants.MDC_HTTP_ENDPOINT, request.getRequestURL().toString());
		MDC.put(MDCConstants.MDC_HTTP_OPERATION, request.getMethod());
		return true;
	}
}