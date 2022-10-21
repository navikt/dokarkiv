package no.nav.dokarkiv;

import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.inject.Inject;
import javax.transaction.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {Application.class, TokenGeneratorConfiguration.class, TestConfig.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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
		var liveness = testRestTemplate.getForEntity("/actuator/health/liveness", String.class);
		assertThat(liveness.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(liveness.getBody()).contains("UP");
	}
}