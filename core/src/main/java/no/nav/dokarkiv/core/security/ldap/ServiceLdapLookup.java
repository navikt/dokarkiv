package no.nav.dokarkiv.core.security.ldap;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * Passes calls to map Ldap Attributes to name
 *
 * @author Martin Burheim Tingstad, Visma Consulting.
 */
public class ServiceLdapLookup extends CommonLdapLookup implements InitializingBean {

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(cache, "must set cacheManager for cache");
	}

//	@Inject
//	public void setCacheManager(EhCacheCacheManager cacheManager) {
//		cache = cacheManager.getCache(LdapConfig.SERVICE_LDAP_CACHE); FIXME
//	}

}
