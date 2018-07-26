package no.nav.dokarkiv.core.security.ldap;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.inject.Inject;

/**
 * Integration test for ServiceLdapLookup
 *
 * @author Tore Gard Andersen
 */
//@ContextConfiguration(classes = {ServiceLdapLookupConfig.class, LdapItestConfig.class})
//@RunWith(SpringJUnit4ClassRunner.class)
@Ignore
public class ServiceLdapLookupTest {

	public static final String ELEPHANT = "elephant";
	public static final String SRV_PENSJON = "srvPensjon";

	@Inject
	private LdapLookup ldapLookup;

	@Inject
	private BrukernavnLdapService brukernavnLdapService;

	@Before
	public void setUp() throws Exception {
//		TestCertificates.setupKeyAndTrustStore();
	}

	@Test
	public void shouldFindServiceUserName() throws Exception {
		String nameForUser = ldapLookup.getServiceUserName(SRV_PENSJON).orNull();
		assertThat(nameForUser, is("srvPensjon"));
	}

	@Test
	public void shouldFindPersonUserName() throws Exception {
		String nameFromService = brukernavnLdapService.searchWithRetry("X000001");
		assertThat(nameFromService, is("Pink Panter"));
	}

	@Test
	public void shouldntFindFicticiousName() throws Exception {
		String nameForUser = ldapLookup.getServiceUserName(ELEPHANT).orNull();
		assertThat(nameForUser, nullValue());
	}

	@Test
	public void shouldFallbackOnIdent() throws Exception {
		String nameOrIdent = ldapLookup.getServiceUserName(ELEPHANT).orIdent();
		assertThat(nameOrIdent, is(ELEPHANT));
	}
}
