package no.nav.dokarkiv.core.security;

import static org.apache.commons.codec.digest.DigestUtils.sha512Hex;
import static org.springframework.ldap.query.LdapQueryBuilder.query;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.cache.CacheConfig;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.wss4j.dom.handler.RequestData;
import org.apache.wss4j.dom.message.token.UsernameToken;
import org.apache.wss4j.dom.validate.UsernameTokenValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Component;

/**
 * Autentiserer SOAP UsernameToken med NAV LDAP.
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component
public class NavLdapUsernameTokenValidator extends UsernameTokenValidator {
	private final String serviceuserBasedn;
	private final LdapTemplate ldapTemplate;
	private final Cache usernameTokenCache;

	public NavLdapUsernameTokenValidator(@Value("${ldap.serviceuser.basedn}") String serviceuserBasedn,
										 LdapTemplate ldapTemplate,
										 CacheManager cacheManager) {
		this.serviceuserBasedn = serviceuserBasedn;
		this.ldapTemplate = ldapTemplate;
		this.usernameTokenCache = cacheManager.getCache(CacheConfig.USERNAME_TOKEN_CACHE);
	}

	// Hvorfor cacher vi dette in-memory?
	// Tjenestene våre som kaller SOAP grensesnittene kjører som batch.
	// Under batchkjøring, uten caching så kan det spamme ned LDAP og Active Directory.
	// Derfor cacher vi UsernameTokens i en kort periode (det er en systembruker som kaller disse)
	@Override
	protected void verifyPlaintextPassword(UsernameToken usernameToken, RequestData data) throws WSSecurityException {
		String username = usernameToken.getName();
		String password = usernameToken.getPassword();
		String sha512Username = sha512Hex(usernameToken.getName());
		String sha512CachedUsernamePassword = usernameTokenCache.get(sha512Username, String.class);
		if (sha512CachedUsernamePassword == null) {
			authenticateLdap(sha512Username, username, password);
		} else {
			authenticateCachedEntry(sha512CachedUsernamePassword, username, password);
		}
	}

	private void authenticateLdap(String sha512Username, String username, String password) throws WSSecurityException {
		try {
			ldapTemplate.authenticate(query().base(serviceuserBasedn).where("cn").is(username), password);
			usernameTokenCache.put(sha512Username, sha512Hex(usernamePassword(username, password)));
		} catch (NamingException e) {
			throw new WSSecurityException(WSSecurityException.ErrorCode.FAILED_AUTHENTICATION);
		}
	}

	private void authenticateCachedEntry(String sha512Cached, String username, String password) throws WSSecurityException {
		if (!sha512Cached.equals(sha512Hex(usernamePassword(username, password)))) {
			throw new WSSecurityException(WSSecurityException.ErrorCode.FAILED_AUTHENTICATION);
		}
	}

	private String usernamePassword(String username, String password) {
		return username + ":" + password;
	}
}
