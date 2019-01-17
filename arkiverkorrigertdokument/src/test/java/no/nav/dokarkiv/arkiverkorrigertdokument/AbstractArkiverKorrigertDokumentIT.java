package no.nav.dokarkiv.arkiverkorrigertdokument;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static no.nav.dokarkiv.arkiverkorrigertdokument.util.TestUtils.DOKUMENTINFO_ID;
import static no.nav.dokarkiv.arkiverkorrigertdokument.util.TestUtils.JOURNALPOST_ID;
import static no.nav.dokarkiv.core.security.JwtClaimsBuilderProvider.openAmClaimsBuilder;
import static no.nav.dokarkiv.core.util.ConverterUtils.objectToJsonString;
import static no.nav.dokarkiv.core.util.TestDataUtils.createAksjonsLoggRequest;

import no.nav.dokarkiv.core.CoreConfig;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.repository.AksjonsLoggRepository;
import no.nav.dokarkiv.core.repository.BegrensningRepository;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.dokarkiv.core.stelvio.SimpleRequestContext;
import no.nav.freg.security.test.oidc.tools.OidcTestService;
import no.nav.freg.security.test.oidc.tools.TestToolsAutoConfig;
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
import org.springframework.transaction.annotation.Transactional;
import wiremock.com.google.common.io.Resources;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		classes = {CoreConfig.class, ArkiverKorrigertDokumentConfig.class, TestToolsAutoConfig.class})
@ActiveProfiles("itest,wiremock,ldap,oidc")
@AutoConfigureDataJpa
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@AutoConfigureDataLdap
@AutoConfigureWireMock(port = 0)
@Transactional
public abstract class AbstractArkiverKorrigertDokumentIT {

	protected static final String OPPRETTET_KILDE_NAVN = "Opprettet kilde";
	protected static final String TILKNYTTET_AV_NAVN = "Tilknyttetnavn";
	protected static final String URL_ARKIVERKORRIGERTDOKUMENT = "/rest/arkiverkorrigertdokument/";
	protected static final String URL_ANGREARKIVERKORRIGERTDOKUMENT = "/rest/arkiverkorrigertdokument/angre/";
	protected static final String URL_SLETTDOKUMENT = "/rest/logiskslettdokument/";
	private String OIDC_TOKEN_PERSON_USER_TEST;
	private String OIDC_TOKEN_SERVICE_USER_TEST;
	private String OIDC_TOKEN_SERVICE_NO_ACCESS_USER_TEST;
	private String NAV_CONSUMER_TOKEN = "Nav-Consumer-Token";
	private final String SERVICE_USER_ID = "srvjoarkadmin";
	private final String PERSON_USER_ID = "Z990782";
	private final String NO_ACCESS_SERVICE_USER_ID = "srvdokarkiv";

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	@Inject
	protected JoarkRepository joarkRepository;
	@Inject
	protected JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;
	@Inject
	protected TestRestTemplate restTemplate;
	@Inject
	protected DokumentinfoRepository dokumentinfoRepository;
	@Inject
	protected OidcTestService oidcTestService;
	@Inject
	protected DokumentFilRepository dokumentFilRepository;
	@Inject
	protected BegrensningRepository begrensningRepository;
	@Inject
	protected AksjonsLoggRepository aksjonsLoggRepository;
	@Before
	public void setUp() {
		OIDC_TOKEN_PERSON_USER_TEST = "Bearer " + oidcTestService.createOidc(openAmClaimsBuilder().subject(PERSON_USER_ID)
				.build());
		OIDC_TOKEN_SERVICE_USER_TEST = "Bearer " + oidcTestService.createOidc(openAmClaimsBuilder().subject(SERVICE_USER_ID)
				.build());
		OIDC_TOKEN_SERVICE_NO_ACCESS_USER_TEST = "Bearer " + oidcTestService.createOidc(openAmClaimsBuilder().subject(NO_ACCESS_SERVICE_USER_ID)
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

	@Before
	public void cleanup() {
		joarkRepository.deleteAll();
		dokumentinfoRepository.deleteAll();
		journalpostDokumentInfoRelasjonRepository.deleteAll();
		begrensningRepository.deleteAll();
		aksjonsLoggRepository.deleteAll();
	}

	protected HttpEntity createNoAccesHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(HttpHeaders.AUTHORIZATION, OIDC_TOKEN_PERSON_USER_TEST);
		headers.add(NAV_CONSUMER_TOKEN, OIDC_TOKEN_SERVICE_NO_ACCESS_USER_TEST);
		return new HttpEntity(headers);
	}

	protected HttpHeaders createHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(HttpHeaders.AUTHORIZATION, OIDC_TOKEN_PERSON_USER_TEST);
		headers.add(NAV_CONSUMER_TOKEN, OIDC_TOKEN_SERVICE_USER_TEST);
		return headers;
	}

	protected HttpHeaders createHeadersWithAksjon(String aksjon) throws IOException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(HttpHeaders.AUTHORIZATION, OIDC_TOKEN_PERSON_USER_TEST);
		headers.add(NAV_CONSUMER_TOKEN, OIDC_TOKEN_SERVICE_USER_TEST);
		headers.add(AksjonsLoggService.AKSJONS_LOGG_HEADER, objectToJsonString(createAksjonsLoggRequest(JOURNALPOST_ID, DOKUMENTINFO_ID, aksjon)));
		return headers;
	}


	protected HttpHeaders createHeadersNotSrvJoarkadmin() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(HttpHeaders.AUTHORIZATION, OIDC_TOKEN_PERSON_USER_TEST);
		headers.add(NAV_CONSUMER_TOKEN, "Bearer " + oidcTestService.createOidc(openAmClaimsBuilder().subject("srvWrong")
				.build()));
		return headers;
	}

	protected void abacPermit() {
		stubFor(post(urlEqualTo("/abac"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json")));
	}
}
