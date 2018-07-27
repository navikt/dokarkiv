package no.nav.dokarkiv.core.security.ldap;

import static no.nav.dokarkiv.core.cache.CacheConfig.NAVUSER_CACHE;
import static org.springframework.ldap.query.LdapQueryBuilder.query;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class NavUserLdapService {
	private final String navuserBasedn;
	private final LdapTemplate ldapTemplate;

	@Inject
	public NavUserLdapService(@Value("${ldap.navuser.basedn}") String navuserBasedn,
							  LdapTemplate ldapTemplate) {
		this.navuserBasedn = navuserBasedn;
		this.ldapTemplate = ldapTemplate;
	}

	@Retryable(backoff = @Backoff(delay = 500))
	@Cacheable(NAVUSER_CACHE)
	public NavUser findByUserId(final String userId) {
		try {
			return ldapTemplate.findOne(query().base(navuserBasedn).where("cn").is(userId), NavUser.class);
		} catch(IncorrectResultSizeDataAccessException e) {
			// fallback til userId
			return NavUser.builder().userId(userId).build();
		}
	}
}
