package no.nav.dokarkiv.core;

import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.repository.AksjonsLoggRepository;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.DokumentInfoTestRepository;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import no.nav.dokarkiv.core.repository.SakRepository;
import no.nav.dokarkiv.core.skjerming.SkjermingServiceTest;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.dokarkiv.core.stelvio.SimpleRequestContext;
import no.nav.security.mock.oauth2.MockOAuth2Server;
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback;
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.core.AutoConfigureCache;
import org.springframework.boot.test.autoconfigure.data.ldap.AutoConfigureDataLdap;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static no.nav.dokarkiv.core.util.TestDataGenerator.OPPRETTET_KILDE_NAVN;
import static no.nav.dokarkiv.core.util.TestDataUtils.AKSJON_BRUKER;
import static no.nav.dokarkiv.core.util.TestDataUtils.AKSJON_HJEMMEL;
import static no.nav.dokarkiv.core.util.TestDataUtils.AKSJON_MELDING;
import static no.nav.dokarkiv.core.util.TestDataUtils.AKSJON_UTFOERT_AV;

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
	protected JoarkRepository joarkRepository;
	@Autowired
	protected JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;
	@Autowired
	protected DokumentInfoTestRepository dokumentInfoTestRepository;
	@Autowired
	protected TestRestTemplate restTemplate;
	@Autowired
	protected SkjermingService skjermingService;
	@Autowired
	protected SkjermingServiceTest skjermingServiceTest;
	@Autowired
	protected AksjonsLoggRepository aksjonsLoggRepository;
	@Autowired
	protected EntityManager entityManager;
	@Autowired
	protected DokumentFilRepository dokumentFilRepository;
	@Autowired
	protected SakRepository sakRepository;
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

	@AfterEach
	public void cleanup() {
		if (!TestTransaction.isActive()) {
			TestTransaction.start();
		} else {
			TestTransaction.end();
			TestTransaction.start();
		}
		aksjonsLoggRepository.deleteAll();
		dokumentFilRepository.deleteAll();
		journalpostDokumentInfoRelasjonRepository.deleteAll();
		dokumentInfoTestRepository.deleteAll();
		joarkRepository.deleteAll();
		sakRepository.deleteAll();
		TestTransaction.flagForCommit();
		TestTransaction.end();
	}

	protected HttpHeaders createHeadersWithUserAndServiceUserToken() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(HttpHeaders.AUTHORIZATION, BEARER + openAmToken(PERSON_USER_ID));
		headers.add(NAV_CONSUMER_TOKEN, BEARER + restStsToken(SERVICE_USER_ID));
		headers.add(NavHeaders.NAV_CALL_ID, "itest");
		return headers;
	}

	protected HttpHeaders createHeadersWithUserAndServiceUserTokenAndConsumerId() {
		return createHeadersWithUserAndServiceUserTokenAndConsumerId("consumer_id");
	}

	protected HttpHeaders createHeadersWithUserAndServiceUserTokenAndConsumerId(String consumerId) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(openAmToken(PERSON_USER_ID));
		headers.add(NAV_CONSUMER_TOKEN, BEARER + restStsToken(SERVICE_USER_ID));
		headers.add(NavHeaders.NAV_CALL_ID, "Nav-CallId");
		headers.add(NavHeaders.NAV_CONSUMER_ID, consumerId);
		return headers;
	}

	protected HttpHeaders createHeadersWithServiceUserToken() {
		return createHeadersWithServiceUserToken(SERVICE_USER_ID);
	}

	protected HttpHeaders createHeadersWithServiceUserToken(String serviceUserId) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(HttpHeaders.AUTHORIZATION, BEARER + restStsToken(serviceUserId));
		headers.add(NavHeaders.NAV_CALL_ID, "itest");
		return headers;
	}

	protected HttpHeaders createHeadersWithServiceUserTokenAndUserIdHeader(String serviceUserId, String userId) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(HttpHeaders.AUTHORIZATION, BEARER + restStsToken(serviceUserId));
		headers.add(NavHeaders.NAV_CALL_ID, "itest");
		headers.add(NavHeaders.NAV_USER_ID, userId);
		return headers;
	}

	protected HttpHeaders createHeadersWithAksjon() throws IOException {
		HttpHeaders httpHeaders = createHeadersWithUserAndServiceUserToken();
		httpHeaders.add(AksjonsLoggService.AKSJONS_LOGG_BRUKER_HEADER, AKSJON_BRUKER);
		httpHeaders.add(AksjonsLoggService.AKSJONS_LOGG_HJEMMEL_HEADER, AKSJON_HJEMMEL);
		httpHeaders.add(AksjonsLoggService.AKSJONS_LOGG_MELDING_HEADER, AKSJON_MELDING);
		httpHeaders.add(AksjonsLoggService.AKSJONS_LOGG_UTFOERT_AV_HEADER, AKSJON_UTFOERT_AV);
		return httpHeaders;
	}

	protected Journalpost saveJournalpost(Journalpost journalpost) {
		Journalpost newJp = joarkRepository.save(journalpost);

		newJp.getJournalpostDokumentInfoRelasjoner().forEach(rel -> {
			rel.getDokumentInfo().getFildetaljerListe().forEach(filDetaljer -> {
				if (Objects.isNull(dokumentFilRepository.findByFilUuid(filDetaljer.getFilUuid()))) {
					DokumentFil dokumentFil = filDetaljer.createDokumentFil();
					dokumentFil.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
					dokumentFilRepository.save(dokumentFil);
				}
			});
		});
		return newJp;
	}

	protected String restStsToken(String subject) {
		return token("reststs", subject, Map.of());
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
