package no.nav.dokarkiv.core.security.ldap;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

/**
 * Unit test for {@link LdapRetrySearchWrapper}
 * @author Paul Magne Lunde, Visma Consulting
 */
@Ignore
public class LdapRetrySearchWrapperTest {

    private static final String USER_IDENT = "D908765";
    private static final String USER_NAME = "Donald Duck";

    private LdapLookup ldapLookupService = Mockito.mock(ServiceLdapLookup.class);

    private LdapRetrySearchWrapper retrySearchWrapper;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        retrySearchWrapper = new LdapRetrySearchWrapper(USER_IDENT, ldapLookupService);
    }

    @Test
    public void shouldReturnUser() {
        when(ldapLookupService.getNAVIdent(USER_IDENT)).thenReturn(new LdapResponse(USER_IDENT, USER_NAME));
        LdapResponse invoke = retrySearchWrapper.invoke();
        assertNotNull(invoke);
        assertThat(invoke.or(USER_IDENT), is(USER_NAME));
    }

}