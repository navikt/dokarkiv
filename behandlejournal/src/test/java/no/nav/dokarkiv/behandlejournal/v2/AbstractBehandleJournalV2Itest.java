package no.nav.dokarkiv.behandlejournal.v2;

import no.nav.dokarkiv.behandlejournal.TestBehandleConfig;
import no.nav.dokarkiv.core.CoreConfig;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.dokarkiv.core.stelvio.SimpleRequestContext;
import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.BehandleJournalV2;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Date;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
		classes = {CoreConfig.class, BehandleJournalV2Config.class, TokenGeneratorConfiguration.class, TestBehandleConfig.class})
@ActiveProfiles("itest")
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@Transactional
public abstract class AbstractBehandleJournalV2Itest {

	@Inject
	protected BehandleJournalV2 behandleJournalProvider;
	@Inject
    protected JoarkRepositorySkjermet joarkRepository;
	@Inject
    protected DokumentinfoRepository dokumentinfoRepository;
	@Inject
	protected DokumentFilRepository dokumentFilRepository;

	@BeforeEach
	public void setUpItest() {
		joarkRepository.deleteAll();
		dokumentFilRepository.deleteAll();
		RequestContextSetter.setRequestContext(new SimpleRequestContext.Builder()
				.userId("itestuser")
				.componentId("itest")
				.build());
	}

	/**
	 * Uses DateProvider to configure dates.
	 *
	 * @return A testable Joda DateTime.
	 */
	protected DateTime getTodayJodaTime() {
		Date today = DateProvider.getToday();
		return new DateTime(today.getTime());
	}
}
