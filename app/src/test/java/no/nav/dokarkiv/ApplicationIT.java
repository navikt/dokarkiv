package no.nav.dokarkiv;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.security.STSTestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import javax.transaction.Transactional;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {STSTestConfig.class, Application.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("itest")
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@Transactional
public class ApplicationIT {

	@Inject
	private TestRestTemplate testRestTemplate;

	@Test
	public void shouldStartApp() {
		// verifisere at appen klarer starte opp
		String isAlive = testRestTemplate.getForObject("/isAlive", String.class);
		assertThat(isAlive, is("Application is alive!"));
	}
}