package no.nav.dokarkiv.arkiverdokumentproduksjon;

import no.nav.dokarkiv.core.CoreConfig;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.dokarkiv.core.stelvio.SimpleRequestContext;
import no.nav.dokarkiv.core.storage.GoogleCloudBucketStorage;
import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.ArkiverDokumentproduksjonV1;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import javax.transaction.Transactional;

import static org.mockito.Mockito.mock;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
		classes = {AbstractArkiverdokumentproduksjonItest.Config.class, CoreConfig.class,
				ArkiverDokumentproduksjonConfig.class, TokenGeneratorConfiguration.class},
		properties = {"spring.main.allow-bean-definition-overriding=true"})
@ActiveProfiles("itest")
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@Transactional
public abstract class AbstractArkiverdokumentproduksjonItest {

	public static String ITEST_USERID = "itestuser";
	public static String ITEST_COMPONENTID = "itest";

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Inject
	protected ArkiverDokumentproduksjonV1 arkiverDokumentproduksjonProvider;
	@Inject
	protected JoarkRepositorySkjermet joarkRepository;
	@Inject
	protected DokumentinfoRepository dokumentinfoRepository;
	@Inject
	protected DokumentFilRepository dokumentFilRepository;

	@Before
	public void setUpItest() {
		joarkRepository.deleteAll();
		dokumentFilRepository.deleteAll();
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
	}
}
