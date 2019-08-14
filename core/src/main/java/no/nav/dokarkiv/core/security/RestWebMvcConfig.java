package no.nav.dokarkiv.core.security;

import io.micrometer.core.instrument.MeterRegistry;
import no.nav.dokarkiv.core.security.ldap.NavLdapService;
import no.nav.freg.security.oidc.auth.OidcAuthProperties;
import no.nav.freg.security.oidc.auth.idtoken.strategy.IdTokenValidationStrategy;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
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
	private final MeterRegistry meterRegistry;
	private final IdTokenValidationStrategy validationStrategy;
	private final AuthenticationManager authManager;


	public RestWebMvcConfig(NavLdapService navLdapService,
							OidcAuthProperties oidcAuthProperties,
							@Named("basicAuthReadAccessRestInterceptor") HandlerInterceptor basicAuthReadAccessRestInterceptor,
							MeterRegistry meterRegistry, IdTokenValidationStrategy validationStrategy, AuthenticationManager authManager) {
		this.navLdapService = navLdapService;
		this.oidcAuthProperties = oidcAuthProperties;
		this.basicAuthReadAccessRestInterceptor = basicAuthReadAccessRestInterceptor;
		this.meterRegistry = meterRegistry;
		this.validationStrategy = validationStrategy;
		this.authManager = authManager;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(basicAuthReadAccessRestInterceptor)
				.addPathPatterns("/hentjournalsakinfo/**");

		registry.addInterceptor(new ValidateUserAndAddToMDCHandler(navLdapService, meterRegistry))
				.excludePathPatterns(new ArrayList<>(oidcAuthProperties.getIgnoredPaths()))
				.addPathPatterns(oidcAuthProperties.getSecuredPath());

		registry.addInterceptor(new ValidateAdminConsumerAccessInterceptor(navLdapService))
				.addPathPatterns("/rest/admin/**");

		registry.addInterceptor(new ValidateSakApiInterceptor(navLdapService, validationStrategy, authManager))
				.addPathPatterns("/rest/saker/**");

		registry.addInterceptor(new PopulateMDCHandler())
				.addPathPatterns(oidcAuthProperties.getSecuredPath(),
						"/hentjournalsakinfo/**")
				.excludePathPatterns("/rest/saker/**");

	}
}
