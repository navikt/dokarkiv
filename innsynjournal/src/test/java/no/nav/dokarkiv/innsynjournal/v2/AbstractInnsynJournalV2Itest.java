package no.nav.dokarkiv.innsynjournal.v2;

import no.nav.dokarkiv.core.CoreConfig;
import no.nav.dokarkiv.core.consumer.aktoer.AktoerConsumerV2Mock;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.dokarkiv.core.stelvio.SimpleRequestContext;
import no.nav.tjeneste.virksomhet.aktoer.v2.binding.AktoerV2;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.binding.InnsynJournalV2;
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

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
		classes = {CoreConfig.class, AbstractInnsynJournalV2Itest.TestConfig.class, InnsynJournalV2Config.class})
@ActiveProfiles("itest")
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@Transactional
public abstract class AbstractInnsynJournalV2Itest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	@Inject
	protected InnsynJournalV2 innsynJournalV2Provider;
	@Inject
	protected JoarkRepository joarkRepository;
	@Inject
	protected DokumentFilRepository dokumentFilRepository;

	@Configuration
	public static class TestConfig {
		@Bean
		public AktoerV2 aktoerV2() {
			return new AktoerConsumerV2Mock();
		}
	}

	@Before
	public void setUpItest() {
		joarkRepository.deleteAll();
		dokumentFilRepository.deleteAll();
		RequestContextSetter.setRequestContext(new SimpleRequestContext.Builder()
				.userId("itestuser")
				.componentId("itest")
				.build());
	}
}
