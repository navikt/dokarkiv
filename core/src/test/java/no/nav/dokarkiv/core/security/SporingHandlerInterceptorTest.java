package no.nav.dokarkiv.core.security;

import io.micrometer.core.instrument.MeterRegistry;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.NavHeaders;
import no.nav.dokarkiv.core.jaxws.ThreadLocalSubjectHandler;
import no.nav.dokarkiv.core.security.ldap.NavLdapService;
import no.nav.security.token.support.filter.JwtTokenValidationFilter;
import no.nav.security.token.support.spring.EnableJwtTokenValidationConfiguration;
import no.nav.security.token.support.test.JwtTokenGenerator;
import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.MDC;
import org.springframework.boot.test.autoconfigure.data.ldap.DataLdapTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.HandlerMethod;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@RunWith(SpringRunner.class)
@DataLdapTest
@ContextConfiguration(classes = {
        SporingHandlerInterceptorTest.TestConfig.class,
        LdapConfig.class,
        SporingHandlerInterceptor.class,
        NavLdapService.class,
        EnableJwtTokenValidationConfiguration.class,
        TokenGeneratorConfiguration.class})
@ActiveProfiles({"itest", "ldap", "registry"})
public class SporingHandlerInterceptorTest {
    public static final String SERVICE_USER = "srvdokarkiv";
    public static final String USER_ID = "Z990782";
    public static final String USER_NAME = "Stasjonsmester Tidemann";

    @Inject
    private MeterRegistry meterRegistry;

    @Configuration
    @Profile("registry")
    public static class TestConfig {
        @Bean
        public MeterRegistry meterRegistry() {
            return mock(MeterRegistry.class);
        }
    }

    @Inject
    private SporingHandlerInterceptor sporingHandlerInterceptor;
    @Inject
    private JwtTokenValidationFilter validationFilter;
    private final MockFilterChain filterChain = new MockFilterChain() {
        @Override
        public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
            try {
                sporingHandlerInterceptor.preHandle((HttpServletRequest) request, (HttpServletResponse) response, new HandlerMethod(new Object(), "equals", Object.class));
            } catch (Exception e) {
                throw new IllegalStateException();
            }
        }
    };

    @Before
    public void setUp() {
        System.setProperty("no.nav.modig.core.context.subjectHandlerImplementationClass", ThreadLocalSubjectHandler.class.getName());
    }

    @Test
    public void shouldValidateAndAddToMDCWhenOnlyServiceUser() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        request.addHeader(HttpHeaders.AUTHORIZATION, getServiceUserToken());
        HttpServletResponse response = new MockHttpServletResponse();

        validationFilter.doFilter(request, response, filterChain);

        assertThat(MDC.get(MDCConstants.MDC_USER_NAME), is(SERVICE_USER));
        assertThat(MDC.get(MDCConstants.MDC_USER_ID), is(SERVICE_USER));
        assertThat(MDC.get(MDCConstants.MDC_CONSUMER_ID), is(SERVICE_USER));
    }

    @Test
    public void shouldValidateAndAddToMDCWhenNavUserAndServiceUserTokens() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        request.addHeader(HttpHeaders.AUTHORIZATION, getUserToken());
        request.addHeader(NavHeaders.NAV_CONSUMER_TOKEN, getServiceUserToken());
        HttpServletResponse response = new MockHttpServletResponse();

        validationFilter.doFilter(request, response, filterChain);

        assertThat(MDC.get(MDCConstants.MDC_USER_NAME), is(USER_NAME));
        assertThat(MDC.get(MDCConstants.MDC_USER_ID), is(USER_ID));
        assertThat(MDC.get(MDCConstants.MDC_CONSUMER_ID), is(SERVICE_USER));
    }

    @Test
    public void shouldFailOnlyUserToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        request.addHeader(HttpHeaders.AUTHORIZATION, getUserToken());
        MockHttpServletResponse response = new MockHttpServletResponse();

        validationFilter.doFilter(request, response, filterChain);

        assertThat(response.getErrorMessage(), containsString("OIDC token på Authorization header må tilhøre en Servicebruker når Nav-Consumer-Token header ikke er satt"));
    }

    @Test
    public void shouldFailWhenNoToken() throws Exception {
        HttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        MockHttpServletResponse response = new MockHttpServletResponse();

        validationFilter.doFilter(request, response, filterChain);

        assertThat(response.getErrorMessage(), containsString("Finner ingen oidc token på Authorization header. Requesten må enten ha oidc-token for servicebruker på header med key=Authorization og value=Bearer [oidc-token] eller ha oidc-token for internbruker i Authorization header og servicebruker på header med key=Nav-Consumer-Token og value=Bearer [oidc-token]"));
    }

    @Test
    public void shouldFailWhenBothHeadersHaveNavUserToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        request.addHeader(NavHeaders.NAV_CONSUMER_TOKEN, getUserToken());
        request.addHeader(HttpHeaders.AUTHORIZATION, getUserToken());
        MockHttpServletResponse response = new MockHttpServletResponse();

        validationFilter.doFilter(request, response, filterChain);

        assertThat(response.getErrorMessage(), containsString("OIDC token på Nav-Consumer-Token header må tilhøre en Servicebruker når både Authorization og Nav-Consumer-Token header er satt"));
    }

    @Test
    public void shouldFailWhenBothHeadersHaveServiceUserToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader(NavHeaders.NAV_CONSUMER_TOKEN, getServiceUserToken());
        request.addHeader(HttpHeaders.AUTHORIZATION, getServiceUserToken());

        validationFilter.doFilter(request, response, filterChain);

        assertThat(response.getErrorMessage(), containsString("OIDC token på Authorization header må tilhøre en Internbruker når både Authorization og Nav-Consumer-Token header er satt"));
    }

    private String getServiceUserToken() {
        return "Bearer " + getTokenWithSubject(SERVICE_USER);
    }

    private String getUserToken() {
        return "Bearer " + getTokenWithSubject(USER_ID);
    }

    private String getTokenWithSubject(final String subject) {
        return JwtTokenGenerator.createSignedJWT(subject).serialize();
    }
}