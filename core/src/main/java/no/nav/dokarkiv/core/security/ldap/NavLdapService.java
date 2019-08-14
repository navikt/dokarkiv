package no.nav.dokarkiv.core.security.ldap;

import static java.lang.String.format;
import static no.nav.dokarkiv.core.cache.CacheConfig.NAVSERVICEUSER_CACHE;
import static no.nav.dokarkiv.core.cache.CacheConfig.NAVUSER_CACHE;
import static org.springframework.ldap.query.LdapQueryBuilder.query;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.security.AuthenticationResult;
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
@Slf4j
public class NavLdapService {
	private final String navuserBasedn;
	private final String serviceuserBasedn;
	private final LdapTemplate ldapTemplate;

	@Inject
	public NavLdapService(@Value("${ldap.navuser.basedn}") String navuserBasedn, @Value("${ldap.serviceuser.basedn}") String serviceuserBasedn, LdapTemplate ldapTemplate, MeterRegistry meterRegistry) {
		this.navuserBasedn = navuserBasedn;
		this.serviceuserBasedn = serviceuserBasedn;
		this.ldapTemplate = ldapTemplate;
	}


	@Retryable(backoff = @Backoff(delay = 500))
	@Cacheable(NAVUSER_CACHE)
	public NavUser findByUserId(final String userId) {
		try {
			return ldapTemplate.findOne(query().base(navuserBasedn).where("cn").is(userId), NavUser.class);
		} catch(IncorrectResultSizeDataAccessException e) {
			log.warn(format("Feilet ved oppslag av navBruker=%s i LDAP. Feilmelding=%s", userId, e.getMessage()));
			// fallback til userId
			return NavUser.builder().userId(userId).userExistsInLdap(false).build();
		}
	}

	@Retryable(backoff = @Backoff(delay = 500))
	@Cacheable(NAVSERVICEUSER_CACHE)
	public NavUser findByServiceuserId(final String serviceUserId) {
		try {
			return ldapTemplate.findOne(query().base(serviceuserBasedn).where("cn").is(serviceUserId), NavUser.class);
		} catch (IncorrectResultSizeDataAccessException e) {
			log.warn(format("Feilet ved oppslag av servicebruker=%s i LDAP. Feilmelding=%s", serviceUserId, e.getMessage()));
			return NavUser.builder().userId(serviceUserId).userExistsInLdap(false).build();
		}
	}

	public AuthenticationResult authenticateLdapUser(final String userId, final String password) {
		try {
			ldapTemplate.authenticate(query().base(serviceuserBasedn).where("cn").is(userId), password);

			return AuthenticationResult.success(userId, userId);
		} catch (Exception e) {
			return AuthenticationResult.invalid(String.format("Kunne ikke autentisere %s mot %s. %s", userId, serviceuserBasedn, e.getMessage()));
		}

	}

}
