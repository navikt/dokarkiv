package no.nav.dokarkiv.arkiverdokumentproduksjon;

import no.nav.dokarkiv.core.CoreConfig;
import no.nav.dokarkiv.core.consumer.azure.AzureAdGraphService;
import no.nav.dokarkiv.core.repository.DokumentFilTestRepository;
import no.nav.dokarkiv.core.repository.DokumentInfoRepository;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.dokarkiv.core.stelvio.SimpleRequestContext;
import no.nav.dokarkiv.core.storage.GoogleCloudBucketStorage;
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.ArkiverDokumentproduksjonV1;
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
		classes = {AbstractArkiverdokumentproduksjonItest.Config.class, CoreConfig.class,
				ArkiverDokumentproduksjonConfig.class},
		properties = {"spring.main.allow-bean-definition-overriding=true"})
@ActiveProfiles("itest")
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@EnableMockOAuth2Server
@Transactional
public abstract class AbstractArkiverdokumentproduksjonItest {

	public static String ITEST_USERID = "itestuser";
	public static String ITEST_COMPONENTID = "itest";

	@Autowired
	protected ArkiverDokumentproduksjonV1 arkiverDokumentproduksjonProvider;
	@Autowired
	protected JoarkRepositorySkjermet joarkRepository;
	@Autowired
	protected DokumentInfoRepository dokumentInfoRepository;
	@Autowired
	protected DokumentFilTestRepository dokumentFilTestRepository;

	@BeforeEach
	public void setUpItest() {
		joarkRepository.deleteAll();
		dokumentFilTestRepository.deleteAll();
		RequestContextSetter.setRequestContext(new SimpleRequestContext.Builder()
				.userId(ITEST_USERID)
				.componentId(ITEST_COMPONENTID)
				.build());
	}

	@Configuration
	static class Config {
		@Bean
		public GoogleCloudBucketStorage dokprodMellomlagerStorage() {
			return mock(GoogleCloudBucketStorage.class);
		}
		@Bean
		public AzureAdGraphService azureAdGraphService() {
			AzureAdGraphService azureAdGraphServiceMock = mock(AzureAdGraphService.class);
			when(azureAdGraphServiceMock.hentFulltNavn(any())).thenReturn("Username");
			return azureAdGraphServiceMock;
		}
	}
}
