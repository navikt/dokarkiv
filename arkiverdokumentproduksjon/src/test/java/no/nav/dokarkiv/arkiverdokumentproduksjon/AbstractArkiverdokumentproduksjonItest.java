package no.nav.dokarkiv.arkiverdokumentproduksjon;

import jakarta.transaction.Transactional;
import no.nav.dokarkiv.core.CoreConfig;
import no.nav.dokarkiv.core.repository.DokumentFilTestRepository;
import no.nav.dokarkiv.core.repository.DokumentInfoRepository;
import no.nav.dokarkiv.core.repository.JournalpostTestRepository;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.dokarkiv.core.stelvio.SimpleRequestContext;
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.ArkiverDokumentproduksjonV1;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
		classes = {CoreConfig.class,
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
	protected JournalpostTestRepository journalpostTestRepository;
	@Autowired
	protected DokumentInfoRepository dokumentInfoRepository;
	@Autowired
	protected DokumentFilTestRepository dokumentFilTestRepository;

	@BeforeEach
	public void setUpItest() {
		journalpostTestRepository.deleteAll();
		dokumentFilTestRepository.deleteAll();
		RequestContextSetter.setRequestContext(new SimpleRequestContext.Builder()
				.userId(ITEST_USERID)
				.componentId(ITEST_COMPONENTID)
				.build());
	}
}
