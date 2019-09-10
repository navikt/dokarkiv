package no.nav.dokarkiv.arkiverdokumentproduksjon;

import static org.mockito.Mockito.mock;

import no.nav.dokarkiv.core.CoreConfig;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.dokarkiv.core.stelvio.SimpleRequestContext;
import no.nav.dokarkiv.core.storage.Storage;
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
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import javax.transaction.Transactional;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
		classes = {AbstractArkiverdokumentproduksjonItest.Config.class, CoreConfig.class, ArkiverDokumentproduksjonConfig.class},
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
	@Inject
	protected JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;
	@Inject
	protected TransactionTemplate transactionTemplate;

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
		public Storage dokprodMellomlagerStorage() {
			return mock(Storage.class);
		}
	}
}
