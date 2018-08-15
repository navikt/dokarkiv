package no.nav.dokarkiv.core.security.abac;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
@Order(99)
public class SecurityRestConfig extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.antMatcher("/rest/**").authorizeRequests()
				.anyRequest().authenticated().and()
				.addFilterBefore(new OidcTokenAuthenticationFilter(), BasicAuthenticationFilter.class).authorizeRequests()
				.and().csrf().disable(); //Innloggingen er stateless og uten cookies, så dette er trygt.
	}
}