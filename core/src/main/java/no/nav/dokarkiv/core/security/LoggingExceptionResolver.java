package no.nav.dokarkiv.core.security;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoggingExceptionResolver extends AbstractHandlerExceptionResolver {
	@Override
	protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		if (ex instanceof DokarkivFunctionalException) {
			if (logger.isWarnEnabled()) {
				logger.warn(ex.getMessage(), ex);
			}
		} else {
			if (logger.isErrorEnabled()) {
				logger.error(ex.getMessage(), ex);
			}
		}
		return null;
	}
}
