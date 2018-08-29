package no.nav.dokarkiv.core.security.abac;

import no.nav.freg.security.oidc.auth.common.HttpSecurityConfigurer;
import no.nav.freg.security.oidc.config.FregSecurityOidcAutoConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
@Import(value = {FregSecurityOidcAutoConfig.class})
public class SecurityRestConfig {

	@Bean
	public HttpSecurityConfigurer disableCsrfConfigurer() {
		return new HttpSecurityConfigurer() {
			@Override
			public void configure(HttpSecurity http) throws Exception {
				http.csrf().disable();
			}
		};
	}
}