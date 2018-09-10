package no.nav.dokarkiv.core.security;

import no.nav.dokarkiv.core.security.ldap.NavLdapService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.inject.Inject;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Configuration
public class RestWebMvcConfig implements WebMvcConfigurer {

	@Inject
	private NavLdapService navLdapService;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new ValidateUserAndAddToMDCHandler(navLdapService))
				.addPathPatterns("/rest/journalfoerinngaaende/v1/**");
	}

}
