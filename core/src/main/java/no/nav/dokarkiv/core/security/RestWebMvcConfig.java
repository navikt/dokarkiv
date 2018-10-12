package no.nav.dokarkiv.core.security;

import no.nav.dokarkiv.core.security.ldap.NavLdapService;
import no.nav.freg.security.oidc.auth.OidcAuthProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Configuration
public class RestWebMvcConfig implements WebMvcConfigurer {

	private final LdapTemplate ldapTemplate;
	private final NavLdapService navLdapService;
	private final OidcAuthProperties oidcAuthProperties;
	private final CacheManager cacheManager;

	@Value("${ldap.serviceuser.basedn}")
	private String serviceuserBasedn;

	public RestWebMvcConfig(LdapTemplate ldapTemplate,
							NavLdapService navLdapService,
							OidcAuthProperties oidcAuthProperties,
							CacheManager cacheManager) {
		this.ldapTemplate = ldapTemplate;
		this.navLdapService = navLdapService;
		this.oidcAuthProperties = oidcAuthProperties;
		this.cacheManager = cacheManager;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new BasicAuthRestInterceptor(serviceuserBasedn, ldapTemplate, cacheManager))
				.addPathPatterns("/hentjournalsakinfo/**");

		registry.addInterceptor(new ValidateUserAndAddToMDCHandler(navLdapService))
				.excludePathPatterns(new ArrayList<>(oidcAuthProperties.getIgnoredPaths()))
				.addPathPatterns(oidcAuthProperties.getSecuredPath());
	}

}
