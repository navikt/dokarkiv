package no.nav.dokarkiv.core.security.ldap;

import no.nav.dokarkiv.core.security.LdapConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.ldap.DataLdapTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(SpringExtension.class)
@DataLdapTest
@ContextConfiguration(classes = {LdapConfig.class, NavLdapService.class})
@ActiveProfiles({"itest", "ldap"})
public class NavLdapServiceIT {

	@Autowired
	private NavLdapService navLdapService;

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

	@Test
	public void shouldReturnNameWhenServiceUserIdLookedUp() {
		NavUser saksbehandler = navLdapService.findByServiceuserId("srvdokarkiv");
		assertThat(saksbehandler.getFullname(), is("Service account for srvdokarkiv"));
	}

	@Test
	public void shouldReturnNameServiceUserIdWithNoDescriptionLookedUp() {
		NavUser saksbehandler = navLdapService.findByServiceuserId("srvtestersen");
		assertThat(saksbehandler.getFullname(), is("test testersen"));
	}

	@Test
	public void shouldReturnServiceUserIdAsFallbackWhenNotFound() {
		NavUser saksbehandler = navLdapService.findByServiceuserId("abcdefgh");
		assertThat(saksbehandler.getFullname(), is("abcdefgh"));
	}
}
