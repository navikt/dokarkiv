package no.nav.dokarkiv.core;

import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.repository.AksjonsLoggRepository;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import no.nav.dokarkiv.core.repository.SakRepository;
import no.nav.dokarkiv.core.skjerming.SkjermingServiceTest;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.dokarkiv.core.stelvio.SimpleRequestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
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

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.io.IOException;
import java.util.Objects;

import static no.nav.dokarkiv.core.util.TestDataGenerator.OPPRETTET_KILDE_NAVN;
import static no.nav.dokarkiv.core.util.TestDataUtils.AKSJON_BRUKER;
import static no.nav.dokarkiv.core.util.TestDataUtils.AKSJON_HJEMMEL;
import static no.nav.dokarkiv.core.util.TestDataUtils.AKSJON_MELDING;
import static no.nav.dokarkiv.core.util.TestDataUtils.AKSJON_UTFOERT_AV;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@ExtendWith(SpringExtension.class)
@AutoConfigureDataJpa
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@AutoConfigureCache
@AutoConfigureDataLdap
@Transactional
public abstract class AbstractRestIT {
	@Inject
	protected JoarkRepository joarkRepository;
	@Inject
	protected JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;
	@Inject
	protected DokumentinfoRepository dokumentinfoRepository;
	@Inject
	protected TestRestTemplate restTemplate;
	@Inject
	protected SkjermingService skjermingService;
	@Inject
	protected SkjermingServiceTest skjermingServiceTest;
	@Inject
	protected AksjonsLoggRepository aksjonsLoggRepository;
	@Inject
	protected EntityManager entityManager;
	@Inject
	protected DokumentFilRepository dokumentFilRepository;
	@Inject
	protected SakRepository sakRepository;

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
		dokumentinfoRepository.deleteAll();
		joarkRepository.deleteAll();
		sakRepository.deleteAll();
		TestTransaction.flagForCommit();
		TestTransaction.end();
	}

	protected HttpHeaders createHeadersWithUserAndServiceUserToken() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(HttpHeaders.AUTHORIZATION, BEARER + getTokenWithSubject(PERSON_USER_ID));
		headers.add(NAV_CONSUMER_TOKEN, BEARER + getTokenWithSubject(SERVICE_USER_ID));
		headers.add(NavHeaders.NAV_CALL_ID, "itest");
		return headers;
	}

	protected HttpHeaders createHeadersWithUserAndServiceUserTokenAndConsumerId() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(getTokenWithSubject(PERSON_USER_ID));
		headers.add(NAV_CONSUMER_TOKEN, BEARER + getTokenWithSubject(SERVICE_USER_ID));
		headers.add(NavHeaders.NAV_CALL_ID, "Nav-CallId");
		headers.add(NavHeaders.NAV_CONSUMER_ID, "consumer_id");
		return headers;
	}
	protected HttpHeaders createHeadersWithServiceUserToken() throws IOException {
		return createHeadersWithServiceUserToken(SERVICE_USER_ID);
	}

	protected HttpHeaders createHeadersWithServiceUserToken(String serviceUserId) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(HttpHeaders.AUTHORIZATION, BEARER + getTokenWithSubject(serviceUserId));
		headers.add(NavHeaders.NAV_CALL_ID, "itest");
		return headers;
	}

	protected HttpHeaders createHeadersWithServiceUserTokenAndUserIdHeader(String serviceUserId, String userId) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(HttpHeaders.AUTHORIZATION, BEARER + getTokenWithSubject(serviceUserId));
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

	protected String getTokenWithSubject(final String subject) {
		return restTemplate.getForObject("/local/jwt?subject=" + subject, String.class);
	}
}
