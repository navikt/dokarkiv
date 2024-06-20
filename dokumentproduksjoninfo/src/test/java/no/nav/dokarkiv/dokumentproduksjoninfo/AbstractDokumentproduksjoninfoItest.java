package no.nav.dokarkiv.dokumentproduksjoninfo;

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
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import javax.transaction.Transactional;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
		classes = {CoreConfig.class, DokumentproduksjonInfoConfig.class})
@ActiveProfiles("itest")
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@EnableMockOAuth2Server
@Transactional
@AutoConfigureWireMock(port = 0)
public abstract class AbstractDokumentproduksjoninfoItest {

	protected static final String MS_ID_SAKSBEHANDLER = "a123c63a-9821-4637-a23d-b706e5b24809";

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

	protected static void stubMsGraphMemberOfEgenAnsatt(String msUserId) {
		stubMsGraphMemberOf(msUserId, "nav/msgraph-memberof-egenansatt.json");
	}

	protected static void stubMsGraphMemberOf(String msUserId, String bodyFile) {
		stubFor(get("/msgraph/users/" + msUserId + "/memberOf")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile(bodyFile)));
	}
}
