package no.nav.dokarkiv.core.security;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.jaxws.ThreadLocalSubjectHandler;
import no.nav.dokarkiv.core.security.ldap.NavLdapService;
import no.nav.freg.security.oidc.auth.idtoken.extract.NavHttpHeaders;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.MDC;
import org.springframework.boot.test.autoconfigure.data.ldap.DataLdapTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {LdapConfig.class, ValidateUserAndAddToMDCHandler.class, NavLdapService.class})
@DataLdapTest
@ActiveProfiles("itest,ldap")
public class ValidateUserAndAddToMDCHandlerTest {

    protected static final String OIDC_TOKEN_PERSON_USER_TEST = "Bearer eyAidHlwIjogIkpXVCIsICJraWQiOiAiU0gxSWVSU2sxT1VGSDNzd1orRXVVcTE5VHZRPSIsICJhbGciOiAiUlMyNTYiIH0.eyAiYXRfaGFzaCI6ICJLWENReU1JdUNHSkRaTzF3el9LM0d3IiwgInN1YiI6ICJaOTkwNzgyIiwgImF1ZGl0VHJhY2tpbmdJZCI6ICJmOTMzZTgxMy00ZDU5LTRjYjgtYTQ0OC0zMTliY2JlOWIzNTgtMjA0NzIwIiwgImlzcyI6ICJodHRwczovL2lzc28tdC5hZGVvLm5vOjQ0My9pc3NvL29hdXRoMiIsICJ0b2tlbk5hbWUiOiAiaWRfdG9rZW4iLCAiYXVkIjogImlkYS10IiwgImNfaGFzaCI6ICJRNzVsekZVanFlV09pZzNMdWxYOHlRIiwgIm9yZy5mb3JnZXJvY2sub3BlbmlkY29ubmVjdC5vcHMiOiAiMjg4NGFjY2MtYmU4My00MWFkLTk4NTctMWE2MWIyMDIzMTRkIiwgImF6cCI6ICJpZGEtdCIsICJhdXRoX3RpbWUiOiAxNTM1NDY0NDE4LCAicmVhbG0iOiAiLyIsICJleHAiOiAxNTM1NDY4MDE5LCAidG9rZW5UeXBlIjogIkpXVFRva2VuIiwgImlhdCI6IDE1MzU0NjQ0MTkgfQ.K9gDJI97u0A2mbF51qaS66AlXcVdzYYrIoUTXQ-Ol3nOdZ_XAEPSoQLi_uuccaniXZVjGCAOXXNuqdz9A-tY22cbiZ4SZ8HaSIA3WvRUOneES0r2RFg5oN3EAgt3okOHIShkPPjk7UwXqYe4D3dzZE6xaM7UmNMzyetvE4RMcti33bpXevonMxd-qHjWC9MuZBQdPwHvxIYgah0VGSp7WJ4KdizSW3ArPCWgZH-2UDvW8ugFVOigIOcEa93I3_HrBj6dTrlhn43WBo0q0G-Zvu0-Zya3Xts1QkJbRqmc6hpLF2attIPpqw8nwQv3S-gJidx_pLnPHK2OjjQgnMJruw";
    protected static final String OIDC_TOKEN_SERVICE_USER_TEST = "Bearer eyAidHlwIjogIkpXVCIsICJraWQiOiAiU0gxSWVSU2sxT1VGSDNzd1orRXVVcTE5VHZRPSIsICJhbGciOiAiUlMyNTYiIH0.eyAiYXRfaGFzaCI6ICJjWmNWUHFMaURXSzFoTjhRN3RfT0RBIiwgInN1YiI6ICJzcnZkb2thcmtpdiIsICJhdWRpdFRyYWNraW5nSWQiOiAiZjg0ODIxYTktOGZkZS00OTI2LThlYmYtMWZiOTlkMzY5MjE2LTI3MjI3MiIsICJpc3MiOiAiaHR0cHM6Ly9pc3NvLXQuYWRlby5ubzo0NDMvaXNzby9vYXV0aDIiLCAidG9rZW5OYW1lIjogImlkX3Rva2VuIiwgImF1ZCI6ICJmcmVnLXRva2VuLXByb3ZpZGVyLXQwIiwgImNfaGFzaCI6ICIwakloUXd3NnVwU2tnSmY5U1RpemZRIiwgIm9yZy5mb3JnZXJvY2sub3BlbmlkY29ubmVjdC5vcHMiOiAiZTYzZjM4MTUtZTI0OS00Y2RmLTllMDUtYTY4NDc2YzdjYzhmIiwgImF6cCI6ICJmcmVnLXRva2VuLXByb3ZpZGVyLXQwIiwgImF1dGhfdGltZSI6IDE1MzU1Mzg2NzEsICJyZWFsbSI6ICIvIiwgImV4cCI6IDE1MzU1NDIyNzEsICJ0b2tlblR5cGUiOiAiSldUVG9rZW4iLCAiaWF0IjogMTUzNTUzODY3MSB9.rX5trZihldaIny2H9ePl0PoLGR9hPLYogdbnNv68bkRn-5jgX1OsQO3S9hCFUzq4C7jjfVB03aI6Xbx_0SMwf01hrBmQeTGBTLimer_b_rdA6fLxzwc2yek94GhBLwh9hkOyAtHjD4blShag-rxJnE0sgGwTUZ5hqDPRZWPJl9rnCoIBoaLd8qMQLltdy9Wzr_1w1jb8CZOM8gGY-k7jrMlS4ddxZHrQTQhIzUcsEMNRRZW8QlhmGtn-TRPlnYGSoP0HO2oSnMtF5fnw0ui_eQ-Xawy_qojB5RrxqM_-0UMVkHfvWhdDBg6DR4zS1UzbqIidJfHRdu7cidCp7OkA2w";

    @Inject
    private NavLdapService navLdapService;

    private ValidateUserAndAddToMDCHandler validateUserAndAddToMDCHandler;


    @Before
    public void setUp() {
        System.setProperty("no.nav.modig.core.context.subjectHandlerImplementationClass", ThreadLocalSubjectHandler.class.getName());
        validateUserAndAddToMDCHandler = new ValidateUserAndAddToMDCHandler(navLdapService);
    }

    @Test
    public void shouldValidateAndAddToMDCWhenOnlyServiceUser() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, OIDC_TOKEN_SERVICE_USER_TEST);
        HttpServletResponse response = new MockHttpServletResponse();

        Boolean result = validateUserAndAddToMDCHandler.preHandle(request, response, null);
        assertThat(result, is(true));
        assertThat(MDC.get(MDCConstants.MDC_USER_NAME), is("srvdokarkiv"));
        assertThat(MDC.get(MDCConstants.MDC_USER_ID), is("srvdokarkiv"));
        assertThat(MDC.get(MDCConstants.MDC_CONSUMER_ID), is("srvdokarkiv"));
    }

    @Test
    public void shouldValidateAndAddToMDCWhenNavUserAndServiceUserTokens() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, OIDC_TOKEN_PERSON_USER_TEST);
        request.addHeader(NavHttpHeaders.NAV_CONSUMER_TOKEN_HEADER, OIDC_TOKEN_SERVICE_USER_TEST);
        HttpServletResponse response = new MockHttpServletResponse();

        Boolean result = validateUserAndAddToMDCHandler.preHandle(request, response, null);
        assertThat(result, is(true));
        assertThat(MDC.get(MDCConstants.MDC_USER_NAME), is("Stasjonsmester Tidemann"));
        assertThat(MDC.get(MDCConstants.MDC_USER_ID), is("Z990782"));
        assertThat(MDC.get(MDCConstants.MDC_CONSUMER_ID), is("srvdokarkiv"));
    }

    @Test
    public void shouldFailOnlyUserToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, OIDC_TOKEN_PERSON_USER_TEST);
        MockHttpServletResponse response = new MockHttpServletResponse();

        Boolean result = validateUserAndAddToMDCHandler.preHandle(request, response, null);
        assertThat(result, is(false));
        assertThat(response.getErrorMessage(), containsString("OIDC token på Authorization header må tilhøre en Servicebruker når Nav-Consumer-Token header ikke er satt"));
    }

    @Test
    public void shouldFailWhenNoToken() throws Exception {
        HttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        Boolean result = validateUserAndAddToMDCHandler.preHandle(request, response, null);
        assertThat(result, is(false));
        assertThat(response.getErrorMessage(), containsString("Finner ingen oidc token på Authorization header. Requesten må enten ha oidc-token for servicebruker på header med key=Authorization og value=Bearer [oidc-token] eller ha oidc-token for internbruker i Authorization header og servicebruker på header med key=Nav-Consumer-Token og value=Bearer [oidc-token]"));
    }

    @Test
    public void shouldFailWhenBothHeadersHaveNavUserToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(NavHttpHeaders.NAV_CONSUMER_TOKEN_HEADER, OIDC_TOKEN_PERSON_USER_TEST);
        request.addHeader(HttpHeaders.AUTHORIZATION, OIDC_TOKEN_PERSON_USER_TEST);
        MockHttpServletResponse response = new MockHttpServletResponse();

        Boolean result = validateUserAndAddToMDCHandler.preHandle(request, response, null);
        assertThat(result, is(false));
        assertThat(response.getErrorMessage(), containsString("OIDC token på Nav-Consumer-Token header må tilhøre en Servicebruker når både Authorization og Nav-Consumer-Token header er satt"));
    }

    @Test
    public void shouldFailWhenBothHeadersHaveServiceUserToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader(NavHttpHeaders.NAV_CONSUMER_TOKEN_HEADER, OIDC_TOKEN_SERVICE_USER_TEST);
        request.addHeader(HttpHeaders.AUTHORIZATION, OIDC_TOKEN_SERVICE_USER_TEST);

        Boolean result = validateUserAndAddToMDCHandler.preHandle(request, response, null);
        assertThat(result, is(false));
        assertThat(response.getErrorMessage(), containsString("OIDC token på Authorization header må tilhøre en Internbruker når både Authorization og Nav-Consumer-Token header er satt"));
    }

}