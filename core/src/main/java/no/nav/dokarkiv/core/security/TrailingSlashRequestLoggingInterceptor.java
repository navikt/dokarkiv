package no.nav.dokarkiv.core.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
public class TrailingSlashRequestLoggingInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		String path = request.getRequestURI();
		if (path != null && path.length() > 1 && path.endsWith("/")) {
			log.info("Mottok request med trailing slash. method={}, path={}", request.getMethod(), path);
		}
		return true;
	}
}
