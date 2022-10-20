package no.nav.dokarkiv.dokumentproduksjoninfo;

import no.nav.dokarkiv.core.CoreConfig;
import no.nav.dokarkiv.core.TestCoreConfig;
import no.nav.dokarkiv.core.consumer.azure.AzureAdGraphService;
import no.nav.dokarkiv.core.consumer.azure.TokenConsumer;
import no.nav.dokarkiv.core.consumer.azure.TokenResponse;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import no.nav.dokarkiv.core.skjerming.SkjermingServiceTest;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.dokarkiv.core.stelvio.SimpleRequestContext;
import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.DokumentproduksjonInfoV1;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.inject.Inject;
import javax.transaction.Transactional;

import static org.mockito.Mockito.mock;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
		classes = {CoreConfig.class, DokumentproduksjonInfoConfig.class, TokenGeneratorConfiguration.class, TestCoreConfig.class})
@ActiveProfiles("itest")
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@Transactional
public abstract class AbstractDokumentproduksjoninfoItest {

	@Inject
	protected DokumentproduksjonInfoV1 dokumentproduksjonInfoProvider;
	@Inject
	protected JoarkRepositorySkjermet joarkRepository;
	@Inject
	protected DokumentFilRepository dokumentFilRepository;
	@Inject
	protected SkjermingServiceTest skjermingService;


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
