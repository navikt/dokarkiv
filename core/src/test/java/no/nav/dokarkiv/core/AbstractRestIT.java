package no.nav.dokarkiv.core;

import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
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

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static no.nav.dokarkiv.core.NavHeaders.NAV_CALL_ID;
import static no.nav.dokarkiv.core.security.SporingHandlerInterceptor.ISSUER_AAD;
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

	protected static final String BEARER = "Bearer ";
	protected static final String NAV_CONSUMER_TOKEN = "Nav-Consumer-Token";
	protected static final String SERVICE_USER_ID = "srvjoarkadmin";
	protected static final String PERSON_USER_ID = "Z990782";
	protected static final String PERSON_USER_NAME = "Stasjonsmester Tidemann";
	protected static final String NO_ACCESS_SERVICE_USER_ID = "srvdokarkiv";

	protected static final String OPPRETTET_AV_NAVN = "opprettetAvNavn";

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
						.kode("OKO")
						.erGyldig(false)
						.datoTilOgMed(LocalDate.of(2023, 5, 1))
						.build());
		fagomradeTestRepository.persist(
				Fagomrade.builder()
						.kode("PEN")
						.erGyldig(true)
						.build());
		fagomradeTestRepository.persist(
				Fagomrade.builder()
						.kode("SYK")
						.erGyldig(true)
						.build());
		fagomradeTestRepository.persist(
				Fagomrade.builder()
						.kode("SYM")
						.erGyldig(true)
						.build());
		fagomradeTestRepository.persist(
				Fagomrade.builder()
						.kode("TIL")
						.erGyldig(true)
						.build());
		fagomradeTestRepository.persist(
				Fagomrade.builder()
						.kode("UFO")
						.erGyldig(true)
						.build());
		fagomradeTestRepository.persist(
				Fagomrade.builder()
						.kode("FOR")
						.erGyldig(true)
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

	protected HttpHeaders createHeadersWithServiceUserTokenAndClaim( String claim) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(APPLICATION_JSON);
		headers.setBearerAuth(azureTokenWithClaim(SERVICE_USER_ID, claim));
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

	protected HttpHeaders createHeadersWithAksjon() {
		HttpHeaders httpHeaders = createHeadersWithUserAndServiceUserToken();
		httpHeaders.add(AksjonsLoggService.AKSJONS_LOGG_BRUKER_HEADER, AKSJON_BRUKER);
		httpHeaders.add(AksjonsLoggService.AKSJONS_LOGG_HJEMMEL_HEADER, AKSJON_HJEMMEL);
		httpHeaders.add(AksjonsLoggService.AKSJONS_LOGG_MELDING_HEADER, AKSJON_MELDING);
		httpHeaders.add(AksjonsLoggService.AKSJONS_LOGG_UTFOERT_AV_HEADER, AKSJON_UTFOERT_AV);
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

	protected String azureTokenWithClaim(String subject, String role) {
		return token(ISSUER_AAD, subject, Map.of("roles", role));
	}

	protected String openAmToken(String subject) {
		return token("openam", subject, Map.of());
	}

	protected String azureToken(String subject) {
		return token("azurev2", subject, Map.of());
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
}
