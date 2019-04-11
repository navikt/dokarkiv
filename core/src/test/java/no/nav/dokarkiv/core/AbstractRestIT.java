package no.nav.dokarkiv.core;

import static no.nav.dokarkiv.core.security.JwtClaimsBuilderProvider.openAmClaimsBuilder;
import static no.nav.dokarkiv.core.util.ConverterUtils.objectToJsonString;
import static no.nav.dokarkiv.core.util.TestDataGenerator.OPPRETTET_KILDE_NAVN;
import static no.nav.dokarkiv.core.util.TestDataUtils.createAksjonsLoggTOHeader;

import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.repository.AksjonsLoggRepository;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.dokarkiv.core.stelvio.SimpleRequestContext;
import no.nav.freg.security.test.oidc.tools.OidcTestService;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.core.AutoConfigureCache;
import org.springframework.boot.test.autoconfigure.data.ldap.AutoConfigureDataLdap;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.io.IOException;
import java.util.Objects;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@RunWith(SpringRunner.class)
@AutoConfigureDataJpa
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@AutoConfigureCache
@AutoConfigureDataLdap
@Transactional
public abstract class AbstractRestIT {

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	@Inject
	protected JoarkRepository joarkRepository;
	@Inject
	protected JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;
	@Inject
	protected DokumentinfoRepository dokumentinfoRepository;
	@Inject
	protected TestRestTemplate restTemplate;
	@Inject
	protected OidcTestService oidcTestService;
	@Inject
	protected SkjermingService skjermingService;
	@Inject
	protected AksjonsLoggRepository aksjonsLoggRepository;
	@Inject
	protected EntityManager entityManager;
	@Inject
	protected DokumentFilRepository dokumentFilRepository;

	protected static final String BEARER = "Bearer ";
	protected static final String NAV_CONSUMER_TOKEN = "Nav-Consumer-Token";
	protected static final String SERVICE_USER_ID = "srvjoarkadmin";
	protected static final String PERSON_USER_ID = "Z990782";
	protected static final String NO_ACCESS_SERVICE_USER_ID = "srvdokarkiv";

	@BeforeClass
	public static void setupRequestContext() {
		RequestContextSetter.setRequestContext(new SimpleRequestContext.Builder()
				.userId("itestuser")
				.componentId("itest")
				.build());
	}

	@After
	public void cleanup() {
		if (!TestTransaction.isActive()) {
			TestTransaction.start();
		}
		aksjonsLoggRepository.deleteAll();
		dokumentFilRepository.deleteAll();
		journalpostDokumentInfoRelasjonRepository.deleteAll();
		dokumentinfoRepository.deleteAll();
		joarkRepository.deleteAll();
		TestTransaction.flagForCommit();
		TestTransaction.end();
	}

	protected HttpHeaders createHeadersWithUserAndServiceUserToken() throws IOException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(HttpHeaders.AUTHORIZATION, BEARER + oidcTestService.createOidc(openAmClaimsBuilder().subject(PERSON_USER_ID)
				.build()));
		headers.add(NAV_CONSUMER_TOKEN, BEARER + oidcTestService.createOidc(openAmClaimsBuilder().subject(SERVICE_USER_ID)
				.build()));
		return headers;
	}

	protected HttpHeaders createHeadersWithServiceUserToken() throws IOException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(HttpHeaders.AUTHORIZATION, BEARER + oidcTestService.createOidc(openAmClaimsBuilder().subject(SERVICE_USER_ID)
				.build()));
		return headers;
	}

	protected HttpHeaders createHeadersWithAksjon() throws IOException {
		HttpHeaders httpHeaders = createHeadersWithUserAndServiceUserToken();
		httpHeaders.add(AksjonsLoggService.AKSJONS_LOGG_HEADER, objectToJsonString(createAksjonsLoggTOHeader()));
		return httpHeaders;
	}

	protected HttpHeaders createHeadersWithServiceUserToken(String serviceUserId) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(HttpHeaders.AUTHORIZATION, BEARER + oidcTestService.createOidc(openAmClaimsBuilder().subject(serviceUserId)
				.build()));
		return headers;
	}

	protected HttpHeaders createHeadersWithUserAndServiceUserToken(String serviceUserId, String personUserId) throws IOException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(HttpHeaders.AUTHORIZATION, BEARER + oidcTestService.createOidc(openAmClaimsBuilder().subject(personUserId)
				.build()));
		headers.add(NAV_CONSUMER_TOKEN, BEARER + oidcTestService.createOidc(openAmClaimsBuilder().subject(serviceUserId)
				.build()));
		return headers;
	}

	protected void saveJournalpost(Journalpost journalpost) {
		joarkRepository.save(journalpost);

		journalpost.getJournalpostDokumentInfoRelasjoner().forEach(rel -> {
			rel.getDokumentInfo().getFildetaljerListe().forEach(filDetaljer -> {
				if (Objects.isNull(dokumentFilRepository.findByFilUuid(filDetaljer.getFilUuid()))) {
					DokumentFil dokumentFil = filDetaljer.createDokumentFil();
					dokumentFil.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
					dokumentFilRepository.save(dokumentFil);
				}
			});
		});
	}
}
