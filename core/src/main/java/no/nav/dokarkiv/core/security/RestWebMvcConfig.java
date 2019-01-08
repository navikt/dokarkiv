package no.nav.dokarkiv.core.security;

import no.nav.dokarkiv.core.hendelselogg.HendelseloggInterceptor;
import no.nav.dokarkiv.core.security.ldap.NavLdapService;
import no.nav.freg.security.oidc.auth.OidcAuthProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.inject.Inject;
import java.util.ArrayList;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Configuration
public class RestWebMvcConfig implements WebMvcConfigurer {

	@Inject
	private NavLdapService navLdapService;

	@Inject
	private OidcAuthProperties oidcAuthProperties;

	@Inject
	private HendelseloggInterceptor hendelseloggInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new ValidateUserAndAddToMDCHandler(navLdapService))
				.excludePathPatterns(new ArrayList<>(oidcAuthProperties.getIgnoredPaths()))
				.addPathPatterns(oidcAuthProperties.getSecuredPath());

		registry.addInterceptor(new ValidateGraphqlNavConsumerInterceptor())
				.addPathPatterns(
						"/rest/graphql",
						"/rest/arkiverkorrigertdokument/",
						"/rest/arkiverkorrigertdokument/angre/**",
						"/rest/fysiskslettdokument/**",
						"/rest/logiskkassasjon/**",
						"/rest/logiskslettdokument/**",
						"/rest/tidligkassasjon/**");


	}
}
