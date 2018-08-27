package no.nav.dokarkiv.core.mdc;

import lombok.extern.slf4j.Slf4j;
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

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		List<String> headers = getOidcAuthHeaders(request.getHeaders("Authorization"));

		if (headers.isEmpty()) {
//			log.warn("Kunne ikke autorisere forespoersel. Finner ingen header med key=Authorization og value=Bearer *oidcToken*.");
			return true;
		} else {
//			List<String> userIds = headers.stream().map(header -> {
//				String[] subStrings = new String(header).split(',');
//
//
//			}).collect(Collectors.toList());
//
			return true;
		}
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
