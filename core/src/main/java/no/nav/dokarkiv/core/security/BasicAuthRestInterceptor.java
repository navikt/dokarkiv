package no.nav.dokarkiv.core.security;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.util.DecodeUtils.decodeBasicAuth;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.ldap.query.LdapQueryBuilder.query;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.cache.CacheConfig;
import no.nav.dokarkiv.core.exceptions.CouldNotDecodeBasicAuthToken;
import org.slf4j.MDC;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.HardcodedFilter;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Slf4j
public class BasicAuthRestInterceptor implements HandlerInterceptor {

	private static final String BASIC = "Basic";

	private final String serviceuserBasedn;
	private final String requiredGroupMember;
	private final LdapTemplate ldapTemplate;
	private final CacheManager cacheManager;


	@Inject
	public BasicAuthRestInterceptor(String baseDn,
									String serviceuserBasedn,
									String requiredGroupMember,
									LdapTemplate ldapTemplate,
									CacheManager cacheManager) {
		this.serviceuserBasedn = serviceuserBasedn;
		if (requiredGroupMember == null) {
			this.requiredGroupMember = null;
		} else {
			this.requiredGroupMember = requiredGroupMember + "," + baseDn;
		}
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
		} catch (CouldNotDecodeBasicAuthToken e) {
			log.error(e.getMessage(), e.getCause());
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
			return false;
		}

		if (decodedCredentials.length != 2) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Feil format på basic authentication token");
			return false;
		}

		String username = decodedCredentials[0];
		String password = decodedCredentials[1];

		Cache usernameTokenCache = cacheManager.getCache(CacheConfig.USERNAME_TOKEN_CACHE);
		Integer cachedAuthHash = usernameTokenCache.get(username, Integer.class);

		if (cachedAuthHash == null) {
			try {
				authenticateWithLdap(username, password);
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

	private void authenticateWithLdap(String username, String password) {
		if (requiredGroupMember == null) {
			ldapTemplate.authenticate(query().base(serviceuserBasedn).where("cn").is(username), password);
		} else {
			AndFilter filter = new AndFilter();
			filter.and(new EqualsFilter("cn", username));
			filter.and(new HardcodedFilter("(memberOf=" + requiredGroupMember + ")"));
			ldapTemplate.authenticate(query().base(serviceuserBasedn).filter(filter), password);
		}
	}

	private String getBasicAuthToken(HttpServletRequest request) {
		return Optional.ofNullable(request.getHeader(AUTHORIZATION))
				.filter(e -> e.startsWith(BASIC))
				.orElse(null);
	}

	private String[] extractAndDecodeHeader(String header) {
		return decodeBasicAuth(header);
	}
}
