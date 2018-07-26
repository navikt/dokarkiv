package no.nav.dokarkiv.core.security.ldap;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.cache.Cache;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;

/**
 * Unit test for ServiceLdapLookup
 *
 * @author Martin Burheim Tingstad, Visma Consulting
 */
@Ignore
@RunWith(MockitoJUnitRunner.class)
public class LdapLookupTest {

    public static final String IDENT = "elephant";
    public static final String LDAP_NAME = "elephant name";
    public static final String ERROR = "Feil ved søk mot LDAP";

    @Mock
    private LdapTemplate ldapTemplate;
    @InjectMocks
    private ServiceLdapLookup ldapLookup;

    @Mock
    private Cache cache;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private void initLdap(String value) {
        when(ldapTemplate.search(any(LdapQuery.class), any(AttributesMapper.class))).thenReturn(Lists.newArrayList(value));
    }

    @Test
    public void shouldFindWithoutCaching() throws Exception {
        initLdap(LDAP_NAME);

        String nameForUser = ldapLookup.getServiceUserName(IDENT).orIdent();
        assertThat(nameForUser, is(LDAP_NAME));
    }

    @Test
    public void shouldThrowExceptionIfLdapFails() throws Exception {
        thrown.expectMessage(ERROR);

        when(ldapTemplate.search(any(LdapQuery.class), any(AttributesMapper.class)))
                .thenThrow(new RuntimeException("ldap unavailable"));

        ldapLookup.getServiceUserName(IDENT);
    }

}
