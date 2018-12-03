package no.nav.dokarkiv.journal.v3;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import no.nav.dokarkiv.core.CoreConfig;
import no.nav.dokarkiv.core.consumer.aktoer.AktoerConsumerV2Mock;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.DokumentUrlInfoRepositoryBegrenset;
import no.nav.dokarkiv.core.repository.JoarkRepositoryBegrenset;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.dokarkiv.core.stelvio.SimpleRequestContext;
import no.nav.tjeneste.virksomhet.aktoer.v2.binding.AktoerV2;
import no.nav.tjeneste.virksomhet.journal.v3.JournalV3;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
		classes = {CoreConfig.class, AbstractJournalV3Itest.TestConfig.class, JournalV3Config.class})
@ActiveProfiles("itest,wiremock")
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@AutoConfigureWireMock(port = 0)
@Transactional
public abstract class AbstractJournalV3Itest {

	protected static final String INTERN_BRUKER_USER_ID = "internBruker";

	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	@Inject
	protected JournalV3 journalV3Provider;
	@Inject
    protected JoarkRepositoryBegrenset joarkRepository;
	@Inject
	protected DokumentFilRepository dokumentFilRepository;
	@Inject
    protected DokumentUrlInfoRepositoryBegrenset dokumentUrlInfoRepository;

	@Configuration
	public static class TestConfig {
		@Bean
		public AktoerV2 aktoerV2() {
			return new AktoerConsumerV2Mock();
		}
	}

	@Before
	public void setUpItest() {
		dokumentUrlInfoRepository.deleteAll();
		dokumentFilRepository.deleteAll();
		joarkRepository.deleteAll();
		RequestContextSetter.setRequestContext(new SimpleRequestContext.Builder()
				.userId("itestuser")
				.componentId("itest")
				.build());
	}

	protected void abacDeny() {
		stubFor(post(urlEqualTo("/abac"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value()).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).withBodyFile("abac/abac-deny.json")));
	}

	protected void abacPermit() {
		stubFor(post(urlEqualTo("/abac"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value()).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).withBodyFile("abac/abac-permit.json")));
	}

	protected String stringFromClasspath(String resourcename) throws IOException {
		return IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(resourcename));
	}
}
