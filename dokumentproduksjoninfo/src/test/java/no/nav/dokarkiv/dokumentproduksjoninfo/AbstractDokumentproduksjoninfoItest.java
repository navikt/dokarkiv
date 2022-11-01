package no.nav.dokarkiv.dokumentproduksjoninfo;

import no.nav.dokarkiv.core.CoreConfig;
import no.nav.dokarkiv.core.consumer.azure.AzureAdGraphService;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import no.nav.dokarkiv.core.skjerming.SkjermingServiceTest;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.dokarkiv.core.stelvio.SimpleRequestContext;
import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.DokumentproduksjonInfoV1;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import javax.transaction.Transactional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
		classes = {CoreConfig.class, DokumentproduksjonInfoConfig.class, TokenGeneratorConfiguration.class, AbstractDokumentproduksjoninfoItest.Config.class})
@ActiveProfiles("itest")
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@Transactional
public abstract class AbstractDokumentproduksjoninfoItest {

	@Autowired
	protected DokumentproduksjonInfoV1 dokumentproduksjonInfoProvider;
	@Autowired
	protected JoarkRepositorySkjermet joarkRepository;
	@Autowired
	protected DokumentFilRepository dokumentFilRepository;
	@Autowired
	protected SkjermingServiceTest skjermingService;


	@Configuration
	public static class Config {

		@Bean
		public AzureAdGraphService azureAdGraphService() {
			AzureAdGraphService azureAdGraphServiceMock = mock(AzureAdGraphService.class);
			when(azureAdGraphServiceMock.hentFulltNavn(any())).thenReturn("Username");
			return azureAdGraphServiceMock;
		}
	}

	@BeforeEach
	public void setUpItest() {
		joarkRepository.deleteAll();
		dokumentFilRepository.deleteAll();
		RequestContextSetter.setRequestContext(new SimpleRequestContext.Builder()
				.userId("testuser")
				.componentId("itest")
				.build());
	}
}
