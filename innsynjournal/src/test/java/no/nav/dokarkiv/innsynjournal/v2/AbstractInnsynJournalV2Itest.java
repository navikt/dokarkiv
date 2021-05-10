package no.nav.dokarkiv.innsynjournal.v2;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.google.common.collect.Lists;
import no.nav.dokarkiv.core.CoreConfig;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import no.nav.dokarkiv.core.skjerming.SkjermingServiceTest;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.dokarkiv.core.stelvio.SimpleRequestContext;
import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.binding.InnsynJournalV2;
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
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
		classes = {CoreConfig.class, InnsynJournalV2Config.class,
				TokenGeneratorConfiguration.class},
		properties = {"spring.main.allow-bean-definition-overriding=true"})
@ActiveProfiles({"itest", "wiremock"})
@AutoConfigureWireMock(port = 0)
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@Transactional
public abstract class AbstractInnsynJournalV2Itest {
	public static final String CURRENT_IDENT = "11111111111";
	public static final String FAIL_IDENT = "93438778934067";
	public static final List<String> HISTORICAL_IDENTS = Lists.newArrayList("012345678910", "234567810");

	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	@Inject
	protected InnsynJournalV2 innsynJournalV2Provider;
	@Inject
    protected JoarkRepositorySkjermet joarkRepository;
	@Inject
	protected JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;
	@Inject
	protected DokumentinfoRepository dokumentinfoRepository;
	@Inject
	protected DokumentFilRepository dokumentFilRepository;
	@Inject
	protected SkjermingServiceTest skjermingService;
	@Inject
	protected EntityManager entityManager;

	@Before
	public void setUpItest() {
		journalpostDokumentInfoRelasjonRepository.deleteAll();
		dokumentinfoRepository.deleteAll();
		joarkRepository.deleteAll();
		dokumentFilRepository.deleteAll();
		entityManager.flush();
		entityManager.clear();
		WireMock.reset();
		RequestContextSetter.setRequestContext(new SimpleRequestContext.Builder()
				.userId("itestuser")
				.componentId("itest")
				.build());
		restStsToken();
	}

	void restStsToken() {
		stubFor(post(urlEqualTo("/reststs"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("reststs/reststs-happy.json")));
	}

	public void happyPdlHistoriskeIdenterStub() {
		stubFor(post(urlEqualTo("/pdl"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("pdl/pdl-historiskident-happy.json")));
	}

	public void notMatchingPdlHistoriskeIdenterStub() {
		stubFor(post(urlEqualTo("/pdl"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("pdl/pdl-historiskident-notmatching.json")));
	}

	public void notFoundPdlHistoriskeIdenterStub() {
		stubFor(post(urlEqualTo("/pdl"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("pdl/pdl-ident-notfound.json")));
	}

	public void technicalErrorPdlStub() {
		stubFor(post(urlEqualTo("/pdl"))
				.willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));
	}
}
