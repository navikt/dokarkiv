package no.nav.dokarkiv.core.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.HandlerInterceptor;

@Order(Integer.MIN_VALUE)
public class ClearMDCHandler implements HandlerInterceptor {
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		MDC.clear();
	}
}
