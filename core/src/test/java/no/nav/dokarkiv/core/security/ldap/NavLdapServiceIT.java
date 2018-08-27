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
@SpringBootTest(classes = {LdapConfig.class, NavLdapService.class})
@DataLdapTest
@ActiveProfiles("itest,ldap")
public class NavLdapServiceIT {

	@Inject
	private NavLdapService navLdapService;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void shouldReturnNameWhenUserIdLookedUp() {
		NavUser saksbehandler = navLdapService.findByUserId("b133337");
		assertThat(saksbehandler.getFullname(), is("Bjarne Betjent"));
	}

	@Test
	public void shouldReturnNameUserIdWithNoDescriptionLookedUp() {
		NavUser saksbehandler = navLdapService.findByUserId("z000001");
		assertThat(saksbehandler.getFullname(), is("Kaptein Sabeltann"));
	}

	@Test
	public void shouldReturnNameUserIdWithNoDisplayNameLookedUp() {
		NavUser saksbehandler = navLdapService.findByUserId("z000002");
		assertThat(saksbehandler.getFullname(), is("Stasjonsmester Tidemann"));
	}

	@Test
	public void shouldReturnUserIdAsFallbackWhenNotFound() {
		NavUser saksbehandler = navLdapService.findByUserId("abcdefgh");
		assertThat(saksbehandler.getFullname(), is("abcdefgh"));
	}

	@Test
	public void shouldReturnServiceUserIdWhenInDifferentBasedn() {
		NavUser saksbehandler = navLdapService.findByUserId("srvdokarkiv");
		assertThat(saksbehandler.getFullname(), is("srvdokarkiv"));
	}
}
