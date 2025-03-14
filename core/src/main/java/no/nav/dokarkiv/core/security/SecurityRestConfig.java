package no.nav.dokarkiv.core.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.HandlerInterceptor;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

/**
 * Vi bruker spring-security for standard filtre.
 * token-support tar seg av autorisasjon til rest-tjenester.
 * Ellers er det custom interceptorer som tar for seg av autorisasjon. Se {@link RestWebMvcConfig}
 *
 * @see no.nav.dokarkiv.core.security
 */
@Configuration
@EnableWebSecurity
public class SecurityRestConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
		// Appen er tilstandsløs og får ingen http sessions fra nettleser til bruker.
		httpSecurity.csrf(AbstractHttpConfigurer::disable);
		// Disse endepunktene er beskyttet av token-support @Protected
		// Se JwtTokenValidationFilter
		httpSecurity.authorizeRequests()
				.requestMatchers("/rest/journalpostapi/**",
						"/rest/admin/**")
				.permitAll();
		httpSecurity.sessionManagement(configurer ->
				configurer.sessionCreationPolicy(STATELESS));
		return httpSecurity.build();
	}

	@Bean
	HandlerInterceptor basicAuthReadAccessRestInterceptor(LdapTemplate ldapTemplate,
														  CacheManager cacheManager,
														  @Value("${ldap.basedn}") String baseDn,
														  @Value("${ldap.serviceuser.basedn}") String serviceuserBaseDn,
														  @Value("${auth.group.lesetilgang.joark}") String authReadRequiredMemberOf) {
		return new BasicAuthRestInterceptor(baseDn, serviceuserBaseDn, authReadRequiredMemberOf, ldapTemplate, cacheManager);
	}
}