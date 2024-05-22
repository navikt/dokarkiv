package no.nav.dokarkiv.core;

import no.nav.dokarkiv.core.domain.codes.Fagomrade;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.repository.AksjonsLoggTestRepository;
import no.nav.dokarkiv.core.repository.DokumentFilTestRepository;
import no.nav.dokarkiv.core.repository.DokumentInfoTestRepository;
import no.nav.dokarkiv.core.repository.FagomradeTestRepository;
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
import org.springframework.boot.test.autoconfigure.core.AutoConfigureCache;
import org.springframework.boot.test.autoconfigure.data.ldap.AutoConfigureDataLdap;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static no.nav.dokarkiv.core.NavHeaders.NAV_CALL_ID;
import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_LOGG_BRUKER_HEADER;
import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_LOGG_HJEMMEL_HEADER;
import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_LOGG_MELDING_HEADER;
import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_LOGG_UTFOERT_AV_HEADER;
import static no.nav.dokarkiv.core.security.SporingHandlerInterceptor.ISSUER_AZUREV2;
import static no.nav.dokarkiv.core.util.TestDataGenerator.OPPRETTET_KILDE_NAVN;
import static no.nav.dokarkiv.core.util.TestDataUtils.AKSJON_BRUKER;
import static no.nav.dokarkiv.core.util.TestDataUtils.AKSJON_HJEMMEL;
import static no.nav.dokarkiv.core.util.TestDataUtils.AKSJON_MELDING;
import static no.nav.dokarkiv.core.util.TestDataUtils.AKSJON_UTFOERT_AV;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@ExtendWith(SpringExtension.class)
@AutoConfigureDataJpa
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@AutoConfigureCache
@AutoConfigureDataLdap
@Transactional
@EnableMockOAuth2Server
public abstract class AbstractRestIT {

	protected static final String BEARER = "Bearer ";
	protected static final String NAV_CONSUMER_TOKEN = "Nav-Consumer-Token";
	protected static final String SERVICE_USER_ID = "srvjoarkadmin";
	protected static final String SERVICEUSER_IKKE_JOARKADMIN = "srvikkejoarkadmin";
	protected static final String APP_NAME_WITH_NAMESPACE = "teamdokumenthandtering:joarkadmin";
	protected static final String PERSON_USER_ID = "Z990782";
	protected static final String PERSON_USER_NAME = "Stasjonsmester Tidemann";
	protected static final String OPPRETTET_AV_NAVN = "opprettetAvNavn";
	protected static final String DEFAULT_CLAIM_OID = "oid";
	protected static final String DEFAULT_CLAIM_SUB = "sub";
	protected static final String CLAIM_AZP_NAME = "azp_name";
	protected static final String CLAIM_NAVIDENT = "NAVident";
	protected static final String CLAIM_NAME = "name";
	protected static final String ROLES = "roles";
	protected static final String APP_CLAIM_SUB = "a2fb96a7-5294-48ea-a1de-a30599f95eb4";

	protected static final String AZP_NAME_JOARKADMIN = "dev-fss:teamdokumenthandtering:joarkadmin";
	static final String NAV_CUSTOM_CLAIM_AZP_NAME = "azp_name";
	protected static final String AZP_NAME_GOSYS = "dev-fss:isa:gosys-q2";
	protected static final String NAV_USER_ID = "Z991234";
	protected static final String MS_AD_GROUP_ID = "abcd163a-9821-4637-a23d-b706e5b24809";
	protected static final String MS_USER_ID_WITH_GROUP_ACCESS = "a123c63a-9821-4637-a23d-b706e5b24809";
	protected static final String MS_USER_ID_WITHOUT_GROUP_ACCESS = "b999c63a-9821-4637-a23d-b706e5b24809";
	public static final String API_ADMIN_ROLE = "api_admin";

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
	private MockOAuth2Server server;

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

		TestTransaction.flagForCommit();
		TestTransaction.end();
	}

	protected HttpHeaders createHeadersWithUserAndServiceUserToken() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(APPLICATION_JSON);
		headers.setBearerAuth(openAmToken(PERSON_USER_ID));
		headers.add(NAV_CONSUMER_TOKEN, BEARER + restStsToken(SERVICE_USER_ID));
		headers.add(NAV_CALL_ID, "itest");
		return headers;
	}

	protected HttpHeaders createHeadersWithServiceUserToken() {
		return createHeadersWithServiceUserToken(SERVICE_USER_ID);
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

	protected MultiValueMap<String, String> createAksjonslogg() {
		MultiValueMap<String, String> valueMap = new LinkedMultiValueMap<>();
		valueMap.add(AKSJONS_LOGG_BRUKER_HEADER, AKSJON_BRUKER);
		valueMap.add(AKSJONS_LOGG_HJEMMEL_HEADER, AKSJON_HJEMMEL);
		valueMap.add(AKSJONS_LOGG_MELDING_HEADER, AKSJON_MELDING);
		valueMap.add(AKSJONS_LOGG_UTFOERT_AV_HEADER, AKSJON_UTFOERT_AV);

		return valueMap;
	}

	protected HttpHeaders createHeadersWithServiceUserTokenAndRolesClaim(String role) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(APPLICATION_JSON);
		headers.setBearerAuth(azureTokenWithRolesClaim(APP_CLAIM_SUB, role));
		headers.add(NAV_CALL_ID, "itest");
		return headers;
	}

	protected HttpHeaders createHeadersWithOboToken(String azpName, String msUserId) {
		HttpHeaders headers = new HttpHeaders();

		headers.setContentType(APPLICATION_JSON);
		headers.setBearerAuth(azureTokenWithAzpKey(azpName, msUserId));
		headers.add(NAV_CALL_ID, "itest");

		return headers;
	}

	protected HttpHeaders createHeadersWithClientCredentialToken() {
		HttpHeaders headers = new HttpHeaders();

		headers.setContentType(APPLICATION_JSON);
		headers.setBearerAuth(azureTokenForClientCredentialFlow(APP_CLAIM_SUB));
		headers.add(NAV_CALL_ID, "itest");

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

	protected HttpHeaders createHeadersWithAksjonslogg(String azpName, String msUserId) {
		HttpHeaders httpHeaders = createHeadersWithOboToken(azpName, msUserId);
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

	protected String azureTokenWithRolesClaim(String subject, String role) {
		return token(ISSUER_AZUREV2, subject, Map.of(ROLES, role, DEFAULT_CLAIM_OID, subject, NAV_CUSTOM_CLAIM_AZP_NAME, AZP_NAME_JOARKADMIN));
	}

	protected String azureTokenWithAzpKey(String callingApp, String user) {
		return token(Map.of(
						CLAIM_AZP_NAME, callingApp,
						CLAIM_NAVIDENT, NAV_USER_ID,
						DEFAULT_CLAIM_OID, user,
						CLAIM_NAME, "F_Z991234 E_Z991234",
						ROLES, API_ADMIN_ROLE
				)
		);
	}

	protected String azureTokenForClientCredentialFlow(String subject) {
		return token(ISSUER_AZUREV2, subject, Map.of(DEFAULT_CLAIM_SUB, subject, DEFAULT_CLAIM_OID, subject));
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
}
