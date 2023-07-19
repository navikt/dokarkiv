package no.nav.dokarkiv.safintern;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.dokarkiv.core.AbstractRestIT;
import no.nav.dokarkiv.core.CoreConfig;
import no.nav.dokarkiv.core.consumer.azure.AzureAdGraphService;
import no.nav.dokarkiv.core.repository.DokumentFilTestRepository;
import no.nav.dokarkiv.core.repository.DokumentInfoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.test.context.ActiveProfiles;

import static no.nav.dokarkiv.safintern.SafinternConstants.ROLE_CLAIM_TILGANG;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(
		webEnvironment = RANDOM_PORT,
		classes = {CoreConfig.class, SafinternConfig.class, AbstractSafinternTest.Config.class},
		properties = {"spring.main.allow-bean-definition-overriding=true"}
)
@ActiveProfiles({"itest", "wiremock"})
public abstract class AbstractSafinternTest extends AbstractRestIT {

	@Configuration
	static class Config {
		@Bean
		public AzureAdGraphService azureAdGraphService() {
			AzureAdGraphService azureAdGraphServiceMock = mock(AzureAdGraphService.class);
			when(azureAdGraphServiceMock.hentFulltNavn(any())).thenReturn("Username");
			return azureAdGraphServiceMock;
		}
	}

	@Autowired
	protected TestRestTemplate restTemplate;

	@Autowired
	protected DokumentInfoRepository dokumentInfoRepository;

	@Autowired
	protected DokumentFilTestRepository DokumentFilTestRepository;

	@Autowired
	protected ObjectMapper objectMapper;

	@BeforeEach
	public void setUpItest() {

	}

	protected HttpEntity<?> createHeaderEntityMedTilgang() {
		return new HttpEntity<>(createHeadersWithServiceUserTokenAndRolesClaim(ROLE_CLAIM_TILGANG));
	}

	protected HttpEntity<?> createHeaderEntityUtenTilgang() {
		return new HttpEntity<>(createHeadersWithServiceUserTokenAndRolesClaim("nei"));
	}

}
