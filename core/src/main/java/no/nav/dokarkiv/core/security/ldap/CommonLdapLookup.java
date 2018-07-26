package no.nav.dokarkiv.core.security.ldap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Maps Ldap Attributes to name, common code
 *
 * @author Martin Burheim Tingstad, Visma Consulting.
 */
public abstract class CommonLdapLookup implements LdapLookup {

	private static final String BASEDN = "OU=Users,OU=NAV,OU=BusinessUnits";

	protected Cache cache;
	protected String logContext = "";

	@Inject
	private LdapTemplate ldapTemplate;

	@Inject
	private NameMapper nameMapper;

	private AtomicLong misses = new AtomicLong(0);
	private AtomicLong lookups = new AtomicLong(0);
	private AtomicLong time = new AtomicLong(0);

	private static final Logger log = LoggerFactory.getLogger(CommonLdapLookup.class);
	private static final String C12 = "Kan ikke finne innslag for gitt Ident i LDAP";
	private static final String C13 = "Fant flere innslag for gitt Ident i LDAP";
	private static final String ERROR = "Feil ved søk mot LDAP";

	@Override
	public LdapResponse getServiceUserName(String userIdent){
		String value = getResponseFromCacheOrQuery(LdapQueryBuilder.query()
				.filter(new EqualsFilter("cn", userIdent)), userIdent);
		return new LdapResponse(userIdent, value);
	}

	@Override
	public LdapResponse getNAVIdent(String userIdent) {
		String value = getResponseFromCacheOrQuery(
				LdapQueryBuilder.query()
						.base(BASEDN)
						.filter(new EqualsFilter("cn", userIdent))
				, userIdent);
		return new LdapResponse(userIdent, value);
	}

	protected String getResponseFromCacheOrQuery(LdapQuery query, String rawkey) {
		String newValue;
		String key = rawkey.trim();

		String cachedValue = checkCache(key);
		if (cachedValue == null) {
			long start = System.currentTimeMillis();
			List<String> list = new ArrayList<>();
			try {
				list = ldapTemplate.search(query, nameMapper);
			} catch (Exception e) {
//				throw new LDAPUnrecoverableException(ERROR + ",key=" + key, e); FIXME
			}
			time.getAndAdd(System.currentTimeMillis() - start);
			lookups.getAndIncrement();
			if (list.isEmpty()) {
				log.info(C12 + ",key=" + key);
				misses.getAndIncrement();
				newValue = "";
			} else {
				if(list.size() > 1) {
					log.info(C13+ ",key=" + key);
				}
				newValue = list.get(0);
			}
			cache.put(key, newValue);
			cachedValue = newValue;
		}
		return cachedValue.equalsIgnoreCase("") ? null : cachedValue;
	}

	private String checkCache(String key) {
		Cache.ValueWrapper wrapper = cache.get(key);
		if (wrapper != null) {
			return (String) wrapper.get();
		}
		return null;
	}

	public void setLogContext(String logContext) {
		this.logContext = logContext;
	}

	@Override
	public long getMisses() {
		return misses.get();
	}

	@Override
	public long getLookups() {
		return lookups.get();
	}

	@Override
	public long getTime() {
		return time.get();
	}

	@Override
	public void clear() {
		misses.set(0);
		lookups.set(0);
		time.set(0);
	}

}
