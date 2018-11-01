package no.nav.dokarkiv.core.security;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import com.auth0.jwt.JWT;
import lombok.extern.slf4j.Slf4j;
import no.nav.freg.security.oidc.auth.idtoken.extract.HeaderTokenExtractor;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Ketill Fenne, Visma Consulting AS
 */
@Slf4j
public class ValidateGraphqlNavConsumerInterceptor implements HandlerInterceptor {

	private final HeaderTokenExtractor headerTokenExtractor = new HeaderTokenExtractor();

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		if (response.getStatus() != HttpStatus.OK.value()) {
			//This means that the validation of oidc tokens failed in IdTokenAuthenticationFilter and we should let the handler go through
			return true;
		}

		String navConsumerToken = headerTokenExtractor.getConsumerToken(request);
		String authorizationToken = headerTokenExtractor.getIdToken(request);

		if (isNotEmpty(navConsumerToken)) {
			String consumerID = getSubjectFromToken(navConsumerToken);
			if (!"srvengangsstonad".equalsIgnoreCase(consumerID)) {
				String message = "OIDC token på Nav-Consumer-Token header må tilhøre serviceuser på joarkadmin";
				log.warn(message);
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
				return false;
			}
		} else if (isNotEmpty(authorizationToken)) {
				String consumerID = getSubjectFromToken(authorizationToken);
				if (!"srvengangsstonad".equalsIgnoreCase(consumerID)) {
					String message = "OIDC token på Authorization-header må tilhøre servicebruker på joarkadmin";
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

	private String getSubjectFromToken(String token) {
		if (isEmpty(token)) {
			return null;
		}
		return JWT.decode(token).getSubject();
	}
}
