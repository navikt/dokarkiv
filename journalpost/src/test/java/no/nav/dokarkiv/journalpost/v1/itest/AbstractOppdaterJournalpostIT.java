package no.nav.dokarkiv.journalpost.v1.itest;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static no.nav.dokarkiv.core.security.JwtClaimsBuilderProvider.openAmClaimsBuilder;

import com.auth0.jwt.JWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.dokarkiv.core.AbstractRestIT;
import no.nav.dokarkiv.core.CoreConfig;
import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import no.nav.dokarkiv.core.repository.SkannetInnholdRepository;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.dokarkiv.core.stelvio.SimpleRequestContext;
import no.nav.dokarkiv.journalpost.v1.JournalpostConfig;
import no.nav.freg.security.test.oidc.tools.OidcTestService;
import no.nav.freg.security.test.oidc.tools.TestToolsAutoConfig;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.data.ldap.AutoConfigureDataLdap;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;
import wiremock.com.google.common.io.Resources;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		classes = {CoreConfig.class, JournalpostConfig.class, TestToolsAutoConfig.class})
@ActiveProfiles("itest,wiremock,ldap,oidc")
@AutoConfigureDataJpa
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@AutoConfigureDataLdap
@AutoConfigureWireMock(port = 0)
@Transactional
public abstract class AbstractOppdaterJournalpostIT extends AbstractRestIT {

	protected String OIDC_TOKEN_PERSON_USER_TEST;
	protected String OIDC_TOKEN_SERVICE_USER_TEST;
	protected String NAV_CONSUMER_TOKEN = "Nav-Consumer-Token";
	protected final String SERVICE_USER_ID = "srvdokarkiv";
	protected final String PERSON_USER_ID = "Z990782";
	protected static final String JOURNALFOER_INNGAAENDE_V1_JOURNALPOSTER = "/rest/v1/journalpost/";
	protected static final ObjectMapper mapper = new ObjectMapper();
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	@Inject
	protected JoarkRepositorySkjermet joarkRepository;
	@Inject
	protected SkannetInnholdRepository skannetInnholdRepository;
	@Inject
	protected TestRestTemplate restTemplate;
	@Inject
    protected DokumentinfoRepository dokumentinfoRepository;
	@Inject
	protected JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;
	@Inject
	protected OidcTestService oidcTestService;

	@Before
	public void setUp() {
		OIDC_TOKEN_PERSON_USER_TEST = "Bearer " + oidcTestService.createOidc(openAmClaimsBuilder().subject(PERSON_USER_ID)
				.build());
		OIDC_TOKEN_SERVICE_USER_TEST = "Bearer " + oidcTestService.createOidc(openAmClaimsBuilder().subject(SERVICE_USER_ID)
				.build());
	}

	@BeforeClass
	public static void setupItest() {
		RequestContextSetter.setRequestContext(new SimpleRequestContext.Builder()
				.userId("itestuser")
				.componentId("itest")
				.build());
	}

	public static String classpathToString(String path) {
		return resourceUrlToString(Resources.getResource(path));
	}

	public static String resourceUrlToString(URL url) {
		try {
			return Resources.toString(url, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException("Could not convert url to String" + url);
		}
	}

	protected Journalpost buildAndCommit(final JournalpostBuilder builder) {
		Journalpost journalpost = joarkRepository.save(builder.build());
		TestTransaction.flagForCommit();
		TestTransaction.end();
		return journalpost;
	}

	protected HttpEntity createHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		headers.add(HttpHeaders.AUTHORIZATION, OIDC_TOKEN_PERSON_USER_TEST);
		headers.add(NAV_CONSUMER_TOKEN, OIDC_TOKEN_SERVICE_USER_TEST);
		return new HttpEntity(headers);
	}

	protected HttpHeaders oidcHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(HttpHeaders.AUTHORIZATION, OIDC_TOKEN_PERSON_USER_TEST);
		headers.add(NAV_CONSUMER_TOKEN, OIDC_TOKEN_SERVICE_USER_TEST);
		return headers;
	}

	protected void abacDeny() {
		stubFor(post(urlEqualTo("/abac"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-deny.json")));
	}

	protected void abacPermit() {
		stubFor(post(urlEqualTo("/abac"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json")));
	}

	protected String stringFromClasspath(String resourcename) throws IOException {
		return IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(resourcename));
	}

	protected String getOidcTokenBody(String oidcToken) {
		return JWT.decode(oidcToken).getPayload();
	}
}
