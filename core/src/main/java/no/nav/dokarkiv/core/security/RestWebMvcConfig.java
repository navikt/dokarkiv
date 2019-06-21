package no.nav.dokarkiv.core.security;

import no.nav.dokarkiv.core.security.ldap.NavLdapService;
import no.nav.freg.security.oidc.auth.OidcAuthProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.inject.Named;
import java.util.ArrayList;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Configuration
public class RestWebMvcConfig implements WebMvcConfigurer {

	private final NavLdapService navLdapService;
	private final OidcAuthProperties oidcAuthProperties;
	private final HandlerInterceptor basicAuthReadAccessRestInterceptor;
	private final HandlerInterceptor basicAuthReadAccessRestInterceptorNoGroup;

	public RestWebMvcConfig(NavLdapService navLdapService,
							OidcAuthProperties oidcAuthProperties,
							@Named("basicAuthReadAccessRestInterceptor") HandlerInterceptor basicAuthReadAccessRestInterceptor,
							@Named("basicAuthReadAccessRestInterceptorNoGroup") HandlerInterceptor basicAuthReadAccessRestInterceptorNoGroup
	) {
		this.navLdapService = navLdapService;
		this.oidcAuthProperties = oidcAuthProperties;
		this.basicAuthReadAccessRestInterceptor = basicAuthReadAccessRestInterceptor;
		this.basicAuthReadAccessRestInterceptorNoGroup = basicAuthReadAccessRestInterceptorNoGroup;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(basicAuthReadAccessRestInterceptor)
				.addPathPatterns("/hentjournalsakinfo/**");
		registry.addInterceptor(basicAuthReadAccessRestInterceptorNoGroup)
				.addPathPatterns("/rest/intern/journalpostapi/v1/journalpost/*/tilknyttVedlegg");

		registry.addInterceptor(new ValidateUserAndAddToMDCHandler(navLdapService))
				.excludePathPatterns(new ArrayList<>(oidcAuthProperties.getIgnoredPaths()))
				.addPathPatterns(oidcAuthProperties.getSecuredPath());
		registry.addInterceptor(new ValidateAdminConsumerAccessInterceptor(navLdapService))
				.addPathPatterns("/rest/admin/**");
		registry.addInterceptor(new PopulateMDCHandler())
				.addPathPatterns(oidcAuthProperties.getSecuredPath(),
						"/hentjournalsakinfo/**");

	}
}
