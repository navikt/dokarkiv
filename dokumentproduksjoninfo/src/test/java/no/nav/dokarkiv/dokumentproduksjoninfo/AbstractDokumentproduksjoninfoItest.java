package no.nav.dokarkiv.dokumentproduksjoninfo;

import jakarta.transaction.Transactional;
import no.nav.dokarkiv.core.CoreConfig;
import no.nav.dokarkiv.core.repository.DokumentFilTestRepository;
import no.nav.dokarkiv.core.repository.JournalpostTestRepository;
import no.nav.dokarkiv.core.skjerming.SkjermingServiceTest;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.dokarkiv.core.stelvio.SimpleRequestContext;
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.DokumentproduksjonInfoV1;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.wiremock.spring.EnableWireMock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
		classes = {CoreConfig.class, DokumentproduksjonInfoConfig.class})
@ActiveProfiles("itest")
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@EnableMockOAuth2Server
@Transactional
@EnableWireMock
public abstract class AbstractDokumentproduksjoninfoItest {

	@Autowired
	protected DokumentproduksjonInfoV1 dokumentproduksjonInfoProvider;
	@Autowired
	protected JournalpostTestRepository journalpostTestRepository;
	@Autowired
	protected DokumentFilTestRepository dokumentFilTestRepository;
	@Autowired
	protected SkjermingServiceTest skjermingService;

	@BeforeEach
	public void setUpItest() {
		journalpostTestRepository.deleteAll();
		dokumentFilTestRepository.deleteAll();
		RequestContextSetter.setRequestContext(new SimpleRequestContext.Builder()
				.userId("testuser")
				.componentId("itest")
				.build());
	}
}
