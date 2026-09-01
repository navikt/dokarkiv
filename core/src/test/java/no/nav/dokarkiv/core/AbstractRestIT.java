package no.nav.dokarkiv.core;

import jakarta.persistence.EntityManager;
import no.nav.dokarkiv.core.domain.codes.Fagomrade;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.repository.AksjonsLoggTestRepository;
import no.nav.dokarkiv.core.repository.DokumentFilTestRepository;
import no.nav.dokarkiv.core.repository.DokumentInfoTestRepository;
import no.nav.dokarkiv.core.repository.FagomradeTestRepository;
import no.nav.dokarkiv.core.repository.InnsynTestRepository;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonTestRepository;
import no.nav.dokarkiv.core.repository.JournalpostTestRepository;
import no.nav.dokarkiv.core.repository.SakTestRepository;
import no.nav.dokarkiv.core.repository.UtsendingsInfoTestRepository;
import no.nav.dokarkiv.core.skjerming.SkjermingServiceTest;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.dokarkiv.core.stelvio.SimpleRequestContext;
import no.nav.security.mock.oauth2.MockOAuth2Server;
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback;
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.cache.test.autoconfigure.AutoConfigureCache;
import org.springframework.boot.data.jpa.test.autoconfigure.AutoConfigureDataJpa;
import org.springframework.boot.data.ldap.test.autoconfigure.AutoConfigureDataLdap;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.AutoConfigureTestEntityManager;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static no.nav.dokarkiv.core.NavHeaders.NAV_CALL_ID;
import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_LOGG_BRUKER_HEADER;
import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_LOGG_HJEMMEL_HEADER;
import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_LOGG_MELDING_HEADER;
import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_LOGG_UTFOERT_AV_HEADER;
import static no.nav.dokarkiv.core.security.SporingHandlerInterceptor.ISSUER_AZUREV2;
import static no.nav.dokarkiv.core.util.TestdataFactory.OPPRETTET_KILDE_NAVN;
import static no.nav.dokarkiv.core.util.TestdataFactory.generateInnsynWithDescription;
import static no.nav.dokarkiv.core.util.TestDataUtils.AKSJON_BRUKER;
import static no.nav.dokarkiv.core.util.TestDataUtils.AKSJON_HJEMMEL;
import static no.nav.dokarkiv.core.util.TestDataUtils.AKSJON_MELDING;
import static no.nav.dokarkiv.core.util.TestDataUtils.AKSJON_UTFOERT_AV;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@ExtendWith(SpringExtension.class)
@AutoConfigureDataJpa
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@AutoConfigureCache
@AutoConfigureDataLdap
@AutoConfigureTestRestTemplate
@Transactional
@EnableMockOAuth2Server
public abstract class AbstractRestIT {

	protected static final String BEARER = "Bearer ";
	protected static final String NAV_CONSUMER_TOKEN = "Nav-Consumer-Token";
	protected static final String SERVICE_USER_ID = "srvjoarkadmin";
	protected static final String SERVICEUSER_IKKE_JOARKADMIN = "srvikkejoarkadmin";
	protected static final String PERSON_USER_NAME = "Stasjonsmester Tidemann";
	protected static final String DEFAULT_CLAIM_OID = "oid";
	protected static final String DEFAULT_CLAIM_SUB = "sub";
	protected static final String CLAIM_AZP_NAME = "azp_name";
	protected static final String CLAIM_NAVIDENT = "NAVident";
	protected static final String CLAIM_NAME = "name";
	protected static final String GROUPS = "groups";
	protected static final String ROLES = "roles";
	protected static final String SCOPES = "scp";
	protected static final String APP_CLAIM_SUB = "a2fb96a7-5294-48ea-a1de-a30599f95eb4";

	protected static final String AZP_NAME_JOARKADMIN = "dev-fss:teamdokumenthandtering:joarkadmin";
	protected static final String KILDENAVN_GOSYS = "isa:gosys-q2";
	protected static final String AZP_NAME_GOSYS = "dev-fss:" + KILDENAVN_GOSYS;
	protected static final String NAV_USER_ID = "Z990782";
	protected static final String NAV_USER_NAME = "F_990782 E_990782";
	protected static final String MS_USER_ID_WITH_GROUP_ACCESS = "a123c63a-9821-4637-a23d-b706e5b24809";
	protected static final String MS_USER_ID_WITHOUT_GROUP_ACCESS = "b999c63a-9821-4637-a23d-b706e5b24809";
	public static final String API_ADMIN_ROLE = "api_admin";
	@Value("${dokarkiv.joarkvedlikeholdgroupid}")
	protected String joarkVedlikeholdGruppeId;

	@Autowired
	protected JournalpostTestRepository journalpostTestRepository;
	@Autowired
	protected JournalpostDokumentInfoRelasjonTestRepository journalpostDokumentInfoRelasjonTestRepository;
	@Autowired
	protected DokumentInfoTestRepository dokumentInfoTestRepository;
	@Autowired
	protected TestRestTemplate restTemplate;
	@Autowired
	protected SkjermingService skjermingService;
	@Autowired
	protected SkjermingServiceTest skjermingServiceTest;
	@Autowired
	protected AksjonsLoggTestRepository aksjonsLoggTestRepository;
	@Autowired
	protected EntityManager entityManager;
	@Autowired
	protected DokumentFilTestRepository dokumentFilTestRepository;
	@Autowired
	protected SakTestRepository sakTestRepository;
	@Autowired
	protected UtsendingsInfoTestRepository utsendingsInfoTestRepository;
	@Autowired
	protected FagomradeTestRepository fagomradeTestRepository;
	@Autowired
	protected InnsynTestRepository innsynTestRepository;
	@Autowired
	private MockOAuth2Server server;
	@Autowired
	protected JsonMapper mapper;

	@BeforeAll
	public static void setupRequestContext() {
		RequestContextSetter.setRequestContext(new SimpleRequestContext.Builder()
				.userId("itestuser")
				.componentId("itest")
				.build());
	}

	@BeforeEach
	public void setUp() {
		lagreFagomraader();
	}

	private void lagreFagomraader() {
		fagomradeTestRepository.persist(
				Fagomrade.builder()
						.kode("UKJ")
						.dekode("Ukjent")
						.erGyldig(false)
						.datoTilOgMed(LocalDate.of(2023, 5, 1))
						.build());
		fagomradeTestRepository.persist(
				Fagomrade.builder()
						.kode("PEN")
						.dekode("Pensjon")
						.erGyldig(true)
						.build());
		fagomradeTestRepository.persist(
				Fagomrade.builder()
						.kode("SYK")
						.dekode("Sykepenger")
						.erGyldig(true)
						.build());
		fagomradeTestRepository.persist(
				Fagomrade.builder()
						.kode("SYM")
						.dekode("Sykmelding")
						.erGyldig(true)
						.build());
		fagomradeTestRepository.persist(
				Fagomrade.builder()
						.kode("TIL")
						.dekode("Tiltak")
						.erGyldig(true)
						.build());
		fagomradeTestRepository.persist(
				Fagomrade.builder()
						.kode("UFO")
						.dekode("Uføreytelser")
						.erGyldig(true)
						.build());
		fagomradeTestRepository.persist(
				Fagomrade.builder()
						.kode("FOR")
						.dekode("Foreldrepenger")
						.erGyldig(true)
						.build());
		fagomradeTestRepository.persist(
				Fagomrade.builder()
						.kode("KTR")
						.dekode("Kontroll")
						.erGyldig(true)
						.build());
		fagomradeTestRepository.persist(
				Fagomrade.builder()
						.kode("RPO")
						.dekode("Retting av personopplysninger")
						.erGyldig(false)
						.datoTilOgMed(LocalDate.of(2023, 5, 1))
						.build());

		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();
	}

	@AfterEach
	public void cleanup() {
		if (!TestTransaction.isActive()) {
			TestTransaction.start();
		} else {
			TestTransaction.end();
			TestTransaction.start();
		}

		fagomradeTestRepository.deleteAll();
		utsendingsInfoTestRepository.deleteAll();
		aksjonsLoggTestRepository.deleteAll();
		dokumentFilTestRepository.deleteAll();
		journalpostDokumentInfoRelasjonTestRepository.deleteAll();
		dokumentInfoTestRepository.deleteAll();
		journalpostTestRepository.deleteAll();
		sakTestRepository.deleteAll();
		innsynTestRepository.deleteAll();

		TestTransaction.flagForCommit();
		TestTransaction.end();
	}

	protected HttpHeaders createHeadersWithUserAndServiceUserToken() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(APPLICATION_JSON);
		headers.setBearerAuth(openAmToken(NAV_USER_ID));
		headers.add(NAV_CONSUMER_TOKEN, BEARER + restStsToken(SERVICE_USER_ID));
		headers.add(NAV_CALL_ID, "itest");
		return headers;
	}

	protected HttpHeaders createHeadersWithServiceUserToken() {
		return createHeadersWithServiceUserToken(SERVICE_USER_ID);
	}

	protected HttpHeaders createHeadersWithServiceUserTokenAndUserIdHeader() {
		return createHeadersWithServiceUserTokenAndUserIdHeader(SERVICE_USER_ID, NAV_USER_ID);
	}

	protected HttpHeaders createHeadersWithServiceUserAndAksjonslogg(String servicebruker) {
		var headers = createHeadersWithServiceUserToken(servicebruker);

		headers.addAll(createAksjonslogg());

		return headers;
	}

	protected HttpHeaders createHeadersWithClientCredentialAndAksjonslogg(String role) {
		var headers = createHeadersWithServiceUserTokenAndRolesClaim(role);
		headers.addAll(createAksjonslogg());
		return headers;
	}

	protected HttpHeaders createAksjonslogg() {
		HttpHeaders headers = new HttpHeaders();
		headers.add(AKSJONS_LOGG_BRUKER_HEADER, AKSJON_BRUKER);
		headers.add(AKSJONS_LOGG_HJEMMEL_HEADER, AKSJON_HJEMMEL);
		headers.add(AKSJONS_LOGG_MELDING_HEADER, AKSJON_MELDING);
		headers.add(AKSJONS_LOGG_UTFOERT_AV_HEADER, AKSJON_UTFOERT_AV);
		return headers;
	}

	protected HttpHeaders createHeadersWithServiceUserTokenAndRolesClaim(String role) {
		return createHeadersWithServiceUserTokenAndRolesClaim(AZP_NAME_JOARKADMIN, role);
	}

	protected HttpHeaders createHeadersWithServiceUserTokenAndRolesClaim(String azpname, String role) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(APPLICATION_JSON);
		headers.setBearerAuth(azureTokenForClientCredentialFlow(APP_CLAIM_SUB, Map.of(ROLES, role, DEFAULT_CLAIM_OID, APP_CLAIM_SUB, CLAIM_AZP_NAME, azpname)));
		headers.add(NAV_CALL_ID, "itest");
		return headers;
	}

	protected HttpHeaders createHeadersWithOboToken(String azpName, String msUserId, String... groups) {
		return createHeadersWithOboTokenWithExtraScope(azpName, msUserId, null, groups);
	}

	protected HttpHeaders createHeadersWithOboTokenWithExtraScope(String azpName, String msUserId, String extraScope, String... groups) {
		HttpHeaders headers = new HttpHeaders();

		headers.setContentType(APPLICATION_JSON);
		headers.setBearerAuth(azureTokenForClientCredentialFlow(NAV_USER_ID, Map.of(
				CLAIM_AZP_NAME, azpName,
				CLAIM_NAVIDENT, NAV_USER_ID,
				DEFAULT_CLAIM_OID, msUserId,
				CLAIM_NAME, NAV_USER_NAME,
				GROUPS, List.of(groups),
				SCOPES, ( extraScope == null ? "" : extraScope + " " ) + "defaultaccess"
		)));
		headers.add(NAV_CALL_ID, "itest");

		return headers;
	}

	protected HttpHeaders createHeadersWithClientCredentialToken() {
		HttpHeaders headers = new HttpHeaders();

		headers.setContentType(APPLICATION_JSON);
		headers.setBearerAuth(azureTokenForClientCredentialFlow(APP_CLAIM_SUB, Map.of(DEFAULT_CLAIM_SUB, APP_CLAIM_SUB, DEFAULT_CLAIM_OID, APP_CLAIM_SUB)));
		headers.add(NAV_CALL_ID, "itest");

		return headers;
	}

	protected HttpHeaders createHeadersWithClientCredentialTokenAndNavUserId() {
		HttpHeaders headers = new HttpHeaders();

		headers.setContentType(APPLICATION_JSON);
		headers.setBearerAuth(azureTokenForClientCredentialFlow(APP_CLAIM_SUB, Map.of(DEFAULT_CLAIM_SUB, APP_CLAIM_SUB, DEFAULT_CLAIM_OID, APP_CLAIM_SUB)));
		headers.add(NAV_CALL_ID, "itest");
		headers.add(NavHeaders.NAV_USER_ID, NAV_USER_ID);

		return headers;
	}

	protected HttpHeaders createHeadersWithServiceUserToken(String serviceUserId) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(APPLICATION_JSON);
		headers.setBearerAuth(restStsToken(serviceUserId));
		headers.add(NAV_CALL_ID, "itest");
		return headers;
	}

	protected HttpHeaders createHeadersWithServiceUserTokenAndUserIdHeader(String serviceUserId, String userId) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(APPLICATION_JSON);
		headers.setBearerAuth(restStsToken(serviceUserId));
		headers.add(NAV_CALL_ID, "itest");
		headers.add(NavHeaders.NAV_USER_ID, userId);
		return headers;
	}

	protected HttpHeaders createHeadersWithAksjonslogg(String azpName, String msUserId, String... groups) {
		HttpHeaders httpHeaders = createHeadersWithOboTokenWithExtraScope(azpName, msUserId, "api_admin", groups);
		httpHeaders.addAll(createAksjonslogg());
		return httpHeaders;
	}

	protected Journalpost saveJournalpost(Journalpost journalpost) {
		Journalpost newJp = journalpostTestRepository.persist(journalpost);

		newJp.getJournalpostDokumentInfoRelasjoner().forEach(rel -> rel.getDokumentInfo().getFildetaljerListe().forEach(filDetaljer -> {
			if (Objects.isNull(dokumentFilTestRepository.findByFilUuid(filDetaljer.getFilUuid()))) {
				DokumentFil dokumentFil = filDetaljer.createDokumentFil();
				dokumentFil.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
				dokumentFilTestRepository.persist(dokumentFil);
			}
		}));
		return newJp;
	}

	protected String restStsToken(String subject) {
		return token("reststs", subject, Map.of());
	}

	protected String azureTokenForClientCredentialFlow(String subject, Map<String, Object> claims) {
		return token(ISSUER_AZUREV2, subject, claims);
	}

	protected String openAmToken(String subject) {
		return token("openam", subject, Map.of());
	}

	protected String token(Map<String, Object> claims) {
		String audience = "aud-localhost";
		return server.issueToken(
				ISSUER_AZUREV2,
				"dokarkiv-itest",
				new DefaultOAuth2TokenCallback(
						ISSUER_AZUREV2,
						NAV_USER_ID,
						"JWT",
						List.of(audience),
						claims,
						3600
				)
		).serialize();
	}

	protected String token(String issuer, String subject, Map<String, Object> claims) {
		String audience = "aud-localhost";
		return server.issueToken(
				issuer,
				"dokarkiv-itest",
				new DefaultOAuth2TokenCallback(
						issuer,
						subject,
						"JWT",
						List.of(audience),
						claims,
						3600
				)
		).serialize();
	}

	protected String classpathResourceToString(String path) {
		try {
			return IOUtils.toString(requireNonNull(getClass().getResourceAsStream(path)), UTF_8);
		} catch (IOException e) {
			return null;
		}
	}

	protected void stubNaisTexasToken() {
		stubFor(post("/nais-texas")
			.willReturn(aResponse()
				.withStatus(OK.value())
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("azure/token_response.json")));
	}

	protected void stubNaisTexasExchangeToken() {
		stubFor(post("/nais-texas-exchange")
			.willReturn(aResponse()
				.withStatus(OK.value())
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("azure/token_response.json")));
	}

	public void stubAzure() {
		stubNaisTexasToken();
		stubFor(post("/azure_token")
			.willReturn(aResponse()
				.withStatus(OK.value())
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("azure/token_response.json")));
	}

	protected static void stubMsGraphGetUser(String navIdent) {
		stubFor(get("/msgraph/users?$count=true&$filter=onPremisesSamAccountName%20eq%20%27" + navIdent + "%27&$select=givenname,surname")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("nav/msgraph-users.json")));
	}

	protected void populateInnsyn() {
		generateInnsynWithDescription().forEach(entityManager::persist);
	}
}
