package no.nav.dokarkiv.core.security;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.ldap.query.LdapQueryBuilder.query;

import no.nav.dokarkiv.core.cache.CacheConfig;
import org.slf4j.MDC;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public class BasicAuthRestInterceptor implements HandlerInterceptor {

	private static final String BASIC = "Basic";
	private static final String CHARSET = "UTF-8";

	private final String serviceuserBasedn;
	private final LdapTemplate ldapTemplate;
	private final CacheManager cacheManager;


	@Inject
	public BasicAuthRestInterceptor(String serviceuserBasedn,
									LdapTemplate ldapTemplate,
									CacheManager cacheManager) {
		this.serviceuserBasedn = serviceuserBasedn;
		this.ldapTemplate = ldapTemplate;
		this.cacheManager = cacheManager;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		String token = getBasicAuthToken(request);

		if (token == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Fant ingen basic authentication token i request headeren");
			return false;
		}

		String[] decodedCredentials;
		try {
			decodedCredentials = extractAndDecodeHeader(token);
		} catch (BadCredentialsException e) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
			return false;
		}

		if (decodedCredentials.length != 2) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Feil format på basic authentication token");
			return false;
		}

		String username = decodedCredentials[0];
		String ***passord=gammelt_passord***];

		Cache usernameTokenCache = cacheManager.getCache(CacheConfig.USERNAME_TOKEN_CACHE);
		Integer cachedAuthHash = usernameTokenCache.get(username, Integer.class);

		if (cachedAuthHash == null) {
			try {
				ldapTemplate.authenticate(query().base(serviceuserBasedn).where("cn").is(username), password);
				usernameTokenCache.put(username, Objects.hash(username, password));
			} catch (Exception e) {
				response.sendError(HttpServletResponse.SC_FORBIDDEN, String.format("Innlogging feilet for bruker med navn %s", username));
				return false;
			}
		} else {
			if (cachedAuthHash != Objects.hash(username, password)) {
				response.sendError(HttpServletResponse.SC_FORBIDDEN, String.format("Innlogging feilet for bruker med navn %s", username));
				return false;
			}
		}

		MDC.put(MDC_CONSUMER_ID, username);
		return true;
	}

	private String getBasicAuthToken(HttpServletRequest request) {
		return Optional.ofNullable(request.getHeader(AUTHORIZATION))
				.filter(e -> e.startsWith(BASIC))
				.orElse(null);
	}

	private String[] extractAndDecodeHeader(String header) throws IOException {
		byte[] base64Token = header.substring(6).getBytes(CHARSET);
		byte[] decoded;

		try {
			decoded = Base64.getDecoder().decode(base64Token);
		} catch (IllegalArgumentException e) {
			throw new BadCredentialsException(
					"Kunne ikke dekode basic authentication token");
		}

		String token = new String(decoded, CHARSET);
		int delim = token.indexOf(':');

		if (delim == -1) {
			throw new BadCredentialsException("Ugyldig basic authentication token");
		}
		return new String[]{token.substring(0, delim), token.substring(delim + 1)};
	}
}
