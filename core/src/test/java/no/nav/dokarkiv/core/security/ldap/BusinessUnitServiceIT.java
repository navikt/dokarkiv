package no.nav.dokarkiv.core.security.ldap;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.security.LdapConfig;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.data.ldap.DataLdapTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {LdapConfig.class, BusinessUnitService.class})
@DataLdapTest
@ActiveProfiles("itest,ldap")
public class BusinessUnitServiceIT {

	@Inject
	private BusinessUnitService businessUnitService;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void shouldReturnNameWhenUserIdLookedUp() {
		BusinessUnit saksbehandler = businessUnitService.findByUserId("b133337");
		assertThat(saksbehandler.getFullname(), is("Bjarne Betjent"));
	}

	@Test
	public void shouldReturnNameUserIdWithNoDescriptionLookedUp() {
		BusinessUnit saksbehandler = businessUnitService.findByUserId("z000001");
		assertThat(saksbehandler.getFullname(), is("Kaptein Sabeltann"));
	}

	@Test
	public void shouldReturnNameUserIdWithNoDisplayNameLookedUp() {
		BusinessUnit saksbehandler = businessUnitService.findByUserId("z000002");
		assertThat(saksbehandler.getFullname(), is("Stasjonsmester Tidemann"));
	}

	@Test
	public void shouldReturnUserIdAsFallbackWhenNotFound() {
		BusinessUnit saksbehandler = businessUnitService.findByUserId("abcdefgh");
		assertThat(saksbehandler.getFullname(), is("abcdefgh"));
	}
}
