package no.nav.dokarkiv.core.security;

import no.nav.freg.security.oidc.auth.common.HttpSecurityConfigurer;
import no.nav.freg.security.oidc.config.FregSecurityOidcAutoConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.inject.Named;

@Configuration
@EnableWebSecurity
@Import(value = FregSecurityOidcAutoConfig.class)
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

	@Bean
	@Named("basicAuthReadAccessRestInterceptor")
	HandlerInterceptor basicAuthReadAccessRestInterceptor(LdapTemplate ldapTemplate,
														  CacheManager cacheManager,
														  @Value("${ldap.basedn}") String baseDn,
														  @Value("${ldap.serviceuser.basedn}") String serviceuserBaseDn,
														  @Value("${auth.group.lesetilgang.joark}") String authReadRequiredMemberOf) {
		return new BasicAuthRestInterceptor(baseDn, serviceuserBaseDn, authReadRequiredMemberOf, null,ldapTemplate, cacheManager);
	}

	@Bean
	@Named("basicAuthReadAccessRestInterceptorNoGroup")
	HandlerInterceptor basicAuthReadAccessRestInterceptorNoGroup(LdapTemplate ldapTemplate,
														  CacheManager cacheManager,
														  @Value("${ldap.basedn}") String baseDn,
														  @Value("${ldap.serviceuser.basedn}") String serviceuserBaseDn) {
		return new BasicAuthRestInterceptor(baseDn, serviceuserBaseDn, null, "srvdokarkivproxy", ldapTemplate, cacheManager);
	}
}