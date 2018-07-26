package no.nav.dokarkiv.core.security.ldap;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit test for {@link BrukernavnLdapService}
 *
 * @author Tore Gard Andersen
 * @author Paul Magne Lunde, Visma Consulting
 */
@Ignore
@RunWith(MockitoJUnitRunner.class)
public class BrukernavnLdapServiceTest {

    public static final long WAIT_BETWEEN_MS = 500l;
    public static final int MAX_ATTEMPTS = 2;
    public static final String HAPPY_USERID = "happy";
    public static final String HAPPY_USERID_LDAPNAME = "happyName";
    public static final String UNHAPPY_RECOVERY_CALLBACK = "unhappy_over_max_retry";
    public static final String UNKNOWN_ERROR_USERID = "unknownsErrorUser";

    @Mock
    private LdapLookup ldapLookupService;
    
//    @Mock
//    private RetryTemplate retryTemplateMock;

    private BrukernavnLdapService target;

    @Before
    public void setUp() throws Exception {
//        target = new BrukernavnLdapService(createRetryTemplate(), ldapLookupService);
    }

    @Test
    public void shouldGetLdapName() {
    	when(ldapLookupService.getNAVIdent(HAPPY_USERID)).thenReturn(new LdapResponse(HAPPY_USERID, HAPPY_USERID_LDAPNAME));
    	
        String ldapName = target.searchWithRetry(HAPPY_USERID);
        
        verify(ldapLookupService, times(1)).getNAVIdent(HAPPY_USERID);
        assertThat(ldapName, is(HAPPY_USERID_LDAPNAME));
    }

    @Test
    public void shouldFailAndRetry() throws Exception {
    	when(ldapLookupService.getNAVIdent(UNHAPPY_RECOVERY_CALLBACK)).thenThrow(new RuntimeException("Failing"));
    	
        String ldapName = target.searchWithRetry(UNHAPPY_RECOVERY_CALLBACK);
        
        verify(ldapLookupService, times(2)).getNAVIdent(UNHAPPY_RECOVERY_CALLBACK);
        assertThat(ldapName, is(UNHAPPY_RECOVERY_CALLBACK));
    }
    
    @Test
    public void shouldFailWithUnexpectedFailureWhenRetry() throws Exception {
//    	when(retryTemplateMock.execute(Matchers.<RetryCallback<LdapResponse>>any(), Matchers.<RecoveryCallback<LdapResponse>>any(), any(RetryState.class))).thenThrow(new Exception());
//    	BrukernavnLdapService ldapService = new BrukernavnLdapService(retryTemplateMock, ldapLookupService);
//
//    	String ldapName = ldapService.searchWithRetry(UNKNOWN_ERROR_USERID);
//    	assertThat(ldapName, is(UNKNOWN_ERROR_USERID)); FIXME
    }


//    private RetryTemplate createRetryTemplate() {
//        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
//        fixedBackOffPolicy.setBackOffPeriod(WAIT_BETWEEN_MS);
//
//        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
//        retryPolicy.setMaxAttempts(MAX_ATTEMPTS);
//
//        RetryTemplate retryTemplate = new RetryTemplate();
//        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);
//        retryTemplate.setRetryPolicy(retryPolicy);
//        return retryTemplate;
//    }
}
