package no.nav.dokarkiv.core.security;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

import no.nav.dokarkiv.core.cache.CacheConfig;
import org.apache.cxf.common.security.SimplePrincipal;
import org.apache.cxf.common.security.UsernameToken;
import org.apache.cxf.interceptor.security.AbstractUsernameTokenInInterceptor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.security.auth.Subject;
import java.util.Objects;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class LdapUsernameTokenValidatorInterceptor extends AbstractUsernameTokenInInterceptor {

	private final LdapTemplate ldapTemplate;
	private final CacheManager cacheManager;

	@Inject
	public LdapUsernameTokenValidatorInterceptor(LdapTemplate ldapTemplate, CacheManager cacheManager) {
		this.ldapTemplate = ldapTemplate;
		this.cacheManager = cacheManager;
	}

	@Override
	protected Subject createSubject(UsernameToken token) {
		Cache usernameTokenCache = cacheManager.getCache(CacheConfig.USERNAME_TOKEN_CACHE);
		final String username = token.getName();
		Integer cachedAuthHash = usernameTokenCache.get(username, Integer.class);
		if (cachedAuthHash == null) {
			try {
				ldapTemplate.authenticate(query().where("cn").is(username), token.getPassword());
				usernameTokenCache.put(username, Objects.hash(token.getName(), token.getPassword()));
			} catch (Exception e) {
				innloggingFeilet(token);
			}
		} else {
			cachedAuthenticate(cachedAuthHash, token);
		}

		Subject subject = new Subject();
		subject.getPrincipals().add(new SimplePrincipal(username));
		return subject;
	}

	private void cachedAuthenticate(Integer cachedAuthHash, UsernameToken token) {
		if(cachedAuthHash != Objects.hash(token.getName(), token.getPassword())) {
			innloggingFeilet(token);
		}
	}

	private void innloggingFeilet(UsernameToken token) {
		reportSecurityException("Innlogging med bruker " + token.getName() + " feilet.");
	}
}
