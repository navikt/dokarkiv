package no.nav.dokarkiv.core.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.inject.Named;

@Configuration
@EnableWebSecurity
public class SecurityRestConfig extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/rest/intern/**", "/rest/journalfoerinngaaende/**", "/rest/journalpostapi/**", "/rest/admin/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.antMatcher("/rest/**");
        http.authorizeRequests().anyRequest().authenticated();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    @Bean
    @Named("basicAuthReadAccessRestInterceptor")
    HandlerInterceptor basicAuthReadAccessRestInterceptor(LdapTemplate ldapTemplate,
                                                          CacheManager cacheManager,
                                                          @Value("${ldap.basedn}") String baseDn,
                                                          @Value("${ldap.serviceuser.basedn}") String serviceuserBaseDn,
                                                          @Value("${auth.group.lesetilgang.joark}") String authReadRequiredMemberOf) {
        return new BasicAuthRestInterceptor(baseDn, serviceuserBaseDn, authReadRequiredMemberOf, ldapTemplate, cacheManager);
    }
}