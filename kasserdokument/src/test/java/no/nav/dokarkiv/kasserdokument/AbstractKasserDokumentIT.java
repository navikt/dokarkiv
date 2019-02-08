package no.nav.dokarkiv.kasserdokument;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static no.nav.dokarkiv.core.security.JwtClaimsBuilderProvider.openAmClaimsBuilder;
import static no.nav.dokarkiv.core.util.ConverterUtils.objectToJsonString;
import static no.nav.dokarkiv.core.util.TestDataUtils.createAksjonsLoggRequest;
import static no.nav.dokarkiv.kasserdokument.util.TestUtil.DOKUMENTINFO_ID;
import static no.nav.dokarkiv.kasserdokument.util.TestUtil.JOURNALPOST_ID;

import no.nav.dokarkiv.core.CoreConfig;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.repository.AksjonsLoggRepository;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.dokarkiv.core.stelvio.SimpleRequestContext;
import no.nav.dokarkiv.kasserdokument.util.TestUtil;
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

import javax.inject.Inject;
import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		classes = {CoreConfig.class, KasserDokumentConfig.class, TestToolsAutoConfig.class})
@ActiveProfiles("itest,wiremock,ldap,oidc")
@AutoConfigureDataJpa
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@AutoConfigureDataLdap
@AutoConfigureWireMock(port = 0)
@Transactional
public abstract class AbstractKasserDokumentIT {

	protected static final String OPPRETTET_KILDE_NAVN = "Opprettet kilde";
	protected static final String TILKNYTTET_AV_NAVN = "Tilknyttetnavn";
	protected static final String URL_FYSISKTIDLIGKASSASJON = "/rest/kasserdokument/";
	private static final String NAV_CONSUMER_TOKEN = "Nav-Consumer-Token";
	private static final String SERVICE_USER_ID = "srvjoarkadmin";
	private static final String PERSON_USER_ID = "Z990782";
	private static final String NO_ACCESS_SERVICE_USER_ID = "srvfinnesikke";
	private static final String BEARER = "Bearer ";
	private String oidcTokenPersonUserTest;
	private String oidcTokenServiceUserTest;
	private String oidcTokenServiceNoAccessUserTest;

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	@Inject
	protected DokumentinfoRepository dokumentinfoRepository;
	@Inject
	protected OidcTestService oidcTestService;
	@Inject
	protected SkjermingService skjermingService;
	@Inject
	protected JoarkRepository joarkRepository;
	@Inject
	protected TestRestTemplate restTemplate;
	@Inject
	protected AksjonsLoggRepository aksjonsLoggRepository;

	@Before
	public void setUp() {
		oidcTokenPersonUserTest = BEARER + oidcTestService.createOidc(openAmClaimsBuilder().subject(PERSON_USER_ID)
				.build());
		oidcTokenServiceUserTest = BEARER + oidcTestService.createOidc(openAmClaimsBuilder().subject(SERVICE_USER_ID)
				.build());
		oidcTokenServiceNoAccessUserTest = BEARER + oidcTestService.createOidc(openAmClaimsBuilder().subject(NO_ACCESS_SERVICE_USER_ID)
				.build());
	}

	@BeforeClass
	public static void setupItest() {
		RequestContextSetter.setRequestContext(new SimpleRequestContext.Builder()
				.userId("itestuser")
				.componentId("itest")
				.build());
	}

	@Before
	public void cleanup() {
		joarkRepository.deleteAll();
		dokumentinfoRepository.deleteAll();
		aksjonsLoggRepository.deleteAll();
	}

	protected HttpEntity createHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(HttpHeaders.AUTHORIZATION, oidcTokenPersonUserTest);
		headers.add(NAV_CONSUMER_TOKEN, oidcTokenServiceUserTest);
		return new HttpEntity(headers);
	}

	protected HttpHeaders createHeadersWithAksjon(String aksjon) throws IOException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(HttpHeaders.AUTHORIZATION, oidcTokenPersonUserTest);
		headers.add(NAV_CONSUMER_TOKEN, oidcTokenServiceUserTest);
		headers.add(AksjonsLoggService.AKSJONS_LOGG_HEADER, objectToJsonString(createAksjonsLoggRequest(JOURNALPOST_ID, DOKUMENTINFO_ID, aksjon)));
		headers.add(AksjonsLoggService.AKSJONS_LOGG_HEADER, objectToJsonString(createAksjonsLoggRequest(JOURNALPOST_ID, TestUtil.DOKUMENTINFO_ID, aksjon)));
		return headers;
	}

	protected HttpHeaders createNoAccessHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(HttpHeaders.AUTHORIZATION, oidcTokenPersonUserTest);
		headers.add(NAV_CONSUMER_TOKEN, oidcTokenServiceNoAccessUserTest);
		return headers;
	}

	protected void abacPermit() {
		stubFor(post(urlEqualTo("/abac"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json")));
	}

}
