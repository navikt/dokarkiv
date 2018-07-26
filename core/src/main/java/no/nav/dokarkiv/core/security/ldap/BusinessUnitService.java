package no.nav.dokarkiv.core.security.ldap;

import static no.nav.dokarkiv.core.cache.CacheConfig.BUSINESS_UNIT_CACHE;
import static org.springframework.ldap.query.LdapQueryBuilder.query;

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
public class BusinessUnitService {
	private final LdapTemplate ldapTemplate;

	@Inject
	public BusinessUnitService(LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}

	@Retryable(backoff = @Backoff(delay = 500))
	@Cacheable(BUSINESS_UNIT_CACHE)
	public BusinessUnit findByUserId(final String userId) {
		try {
			return ldapTemplate.findOne(query().where("cn").is(userId), BusinessUnit.class);
		} catch(IncorrectResultSizeDataAccessException e) {
			// fallback til userId
			return BusinessUnit.builder().userId(userId).build();
		}
	}
}
