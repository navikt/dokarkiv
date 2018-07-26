package no.nav.dokarkiv.behandleinngaaendejournal.v1;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import no.nav.dokarkiv.core.CoreConfig;
import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.dokarkiv.core.stelvio.SimpleRequestContext;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.BehandleInngaaendeJournalV1;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
		classes = {CoreConfig.class, BehandleInngaaendeJournalV1Config.class})
@ActiveProfiles("itest,wiremock")
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@AutoConfigureWireMock(port = 0)
@Transactional
public abstract class AbstractBehandleInngaaendeJournalV1Itest {

	protected static final String INTERN_BRUKER_LDAP_NA_USER_ID = "systemDown";
	protected static final String INTERN_BRUKER_USER_ID = "internBruker";
	protected static final String SYSTEMERESSURS_USER_ID = "srvBjoark";

	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	@Inject
	protected BehandleInngaaendeJournalV1 behandleInngaaendeJournalProvider;
	@Inject
	protected JoarkRepository joarkRepository;
	@Inject
	protected DokumentFilRepository dokumentFilRepository;

	@Before
	public void setUpItest() {
		joarkRepository.deleteAll();
		dokumentFilRepository.deleteAll();
		RequestContextSetter.setRequestContext(new SimpleRequestContext.Builder()
				.userId("itestuser")
				.componentId("itest")
				.build());
	}

	protected Journalpost buildAndCommit(final JournalpostBuilder builder) {
		return joarkRepository.save(builder.build());
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

	protected Journalpost getPersistedJournalpostById(Long journalpostId) {
		return joarkRepository.findById(journalpostId).orElse(null);
	}
}
