package no.nav.dokarkiv.journalpost.v1.itest;

import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.util.TestDataGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.OPPHEV_FEILREGISTRERING;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.UKJENT_BRUKER;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.UTGAAR;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.OD;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.U;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.UB;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.I;
import static no.nav.dokarkiv.journalpost.v1.util.AvvikstypeConstants.FEILREGISTRER_SAKSTILKNYTNING;
import static no.nav.dokarkiv.journalpost.v1.util.AvvikstypeConstants.OPPHEV_FEILREGISTRERT_SAKSTILKNYTNING;
import static no.nav.dokarkiv.journalpost.v1.util.AvvikstypeConstants.SETT_STATUS_UTGAAR;
import static no.nav.dokarkiv.journalpost.v1.util.AvvikstypeConstants.SETT_UKJENT_BRUKER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;
import static org.springframework.http.HttpStatus.OK;

public class FeilregistrerIT extends AbstractJournalpostIT {

	private static final String FEILREGISTRER = "/feilregistrer/";
	private static final String HJEMMEL = "ARKL";

	@Test
	public void happyPathFeilregistrer() {
		Journalpost journalpost = TestDataGenerator.createJournalpostWithHoveddokument();
		Long journalpostId = journalpostTestRepository.persist(journalpost).getJournalpostId();

		commitAndStartNewTransaction();

		var requestEntity = new HttpEntity<>(createHeadersWithServiceUserToken());
		ResponseEntity<String> response = restTemplate.exchange(URL_JOURNALPOST + journalpostId + FEILREGISTRER + FEILREGISTRER_SAKSTILKNYTNING, PATCH, requestEntity, String.class);

		assertEquals(OK, response.getStatusCode());

		commitAndStartNewTransaction();

		Journalpost oppdatertJournalpost = journalpostTestRepository.findById(journalpostId).orElseThrow(RuntimeException::new);

		Saksrelasjon saksrelasjon = oppdatertJournalpost.getSaksrelasjon();
		assertEquals(true, saksrelasjon.getFeilregistrert());
		assertEquals(SERVICE_USER_ID, saksrelasjon.getEndretAvNavn());
		assertEquals(SERVICE_USER_ID, saksrelasjon.getEndretKildeNavn());

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();

		assertEquals(1, aksjonsLoggList.size());

		AksjonsLogg aksjonsLogg = aksjonsLoggList.get(0);
		assertEquals(journalpostId, aksjonsLogg.getJournalpostId());
		assertEquals(SERVICE_USER_ID, aksjonsLogg.getUtfoertAv());
		assertEquals(AksjonsTypeCode.FEILREGISTRER_SAKSTILKNYTNING, aksjonsLogg.getAksjon());
		assertEquals(HJEMMEL, aksjonsLogg.getHjemmel());
		assertEquals(1, aksjonsLogg.getArkivElementEndringer().size());
	}

	@Test
	public void happyPathFeilregistrerWithSaksbehandlerToken() {
		Journalpost journalpost = TestDataGenerator.createJournalpostWithHoveddokument();
		Long journalpostId = journalpostTestRepository.persist(journalpost).getJournalpostId();

		commitAndStartNewTransaction();

		var requestEntity = new HttpEntity<>(createHeadersWithUserAndServiceUserToken());
		ResponseEntity<String> response = restTemplate.exchange(URL_JOURNALPOST + journalpostId + FEILREGISTRER + FEILREGISTRER_SAKSTILKNYTNING, PATCH, requestEntity, String.class);

		assertEquals(OK, response.getStatusCode());

		commitAndStartNewTransaction();

		Journalpost oppdatertJournalpost = journalpostTestRepository.findById(journalpostId).orElseThrow(RuntimeException::new);

		Saksrelasjon saksrelasjon = oppdatertJournalpost.getSaksrelasjon();
		assertEquals(true, saksrelasjon.getFeilregistrert());
		assertEquals(PERSON_USER_NAME, saksrelasjon.getEndretAvNavn());
		assertEquals(SERVICE_USER_ID, saksrelasjon.getEndretKildeNavn());
	}

	@Test
	public void happyPathOpphevFeilregistrering() {
		Journalpost journalpost = TestDataGenerator.createJournalpostWithHoveddokument();
		journalpost.getSaksrelasjon().setFeilregistrert(true);
		Long journalpostId = journalpostTestRepository.persist(journalpost).getJournalpostId();

		commitAndStartNewTransaction();

		var requestEntity = new HttpEntity<>(createHeadersWithServiceUserToken());
		ResponseEntity<String> response = restTemplate.exchange(URL_JOURNALPOST + journalpostId + FEILREGISTRER + OPPHEV_FEILREGISTRERT_SAKSTILKNYTNING, PATCH, requestEntity, String.class);

		assertEquals(OK, response.getStatusCode());

		commitAndStartNewTransaction();

		Journalpost oppdatertJournalpost = journalpostTestRepository.findById(journalpostId).orElseThrow(RuntimeException::new);

		assertEquals(oppdatertJournalpost.getSaksrelasjon().getFeilregistrert(), false);

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();

		assertEquals(1, aksjonsLoggList.size());

		AksjonsLogg aksjonsLogg = aksjonsLoggList.get(0);
		assertEquals(journalpostId, aksjonsLogg.getJournalpostId());
		assertEquals(SERVICE_USER_ID, aksjonsLogg.getUtfoertAv());
		assertEquals(OPPHEV_FEILREGISTRERING, aksjonsLogg.getAksjon());
		assertEquals(HJEMMEL, aksjonsLogg.getHjemmel());
		assertEquals(1, aksjonsLogg.getArkivElementEndringer().size());
	}

	@Test
	public void happyPathUkjentBruker() {
		Journalpost journalpost = TestDataGenerator.createJournalpostWithHoveddokument();
		journalpost.setJournalstatus(U);
		Long journalpostId = journalpostTestRepository.persist(journalpost).getJournalpostId();

		commitAndStartNewTransaction();

		var requestEntity = new HttpEntity<>(createHeadersWithServiceUserToken());
		ResponseEntity<String> response = restTemplate.exchange(URL_JOURNALPOST + journalpostId + FEILREGISTRER + SETT_UKJENT_BRUKER, PATCH, requestEntity, String.class);

		assertEquals(OK, response.getStatusCode());

		commitAndStartNewTransaction();

		Journalpost oppdatertJournalpost = journalpostTestRepository.findById(journalpostId).orElseThrow(RuntimeException::new);

		assertEquals(oppdatertJournalpost.getJournalstatus(), UB);

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();

		assertEquals(1, aksjonsLoggList.size());

		AksjonsLogg aksjonsLogg = aksjonsLoggList.get(0);
		assertEquals(journalpostId, aksjonsLogg.getJournalpostId());
		assertEquals(SERVICE_USER_ID, aksjonsLogg.getUtfoertAv());
		assertEquals(UKJENT_BRUKER, aksjonsLogg.getAksjon());
		assertEquals(HJEMMEL, aksjonsLogg.getHjemmel());
		assertEquals(1, aksjonsLogg.getArkivElementEndringer().size());
	}

	@Test
	public void shouldGet405WhenJournalPostHaveStatusUtgaaende() {
		Journalpost journalpost = TestDataGenerator.createJournalpostWithHoveddokument();
		journalpost.setJournalstatus(U);
		Long journalpostId = journalpostTestRepository.persist(journalpost).getJournalpostId();

		commitAndStartNewTransaction();

		var requestEntity = new HttpEntity<>(createHeadersWithServiceUserToken());
		ResponseEntity<String> response = restTemplate.exchange(URL_JOURNALPOST + journalpostId + FEILREGISTRER + SETT_STATUS_UTGAAR, PATCH, requestEntity, String.class);

		assertEquals(METHOD_NOT_ALLOWED, response.getStatusCode());
	}

	@Test
	public void shouldSetUtgaarJournalstatusWhenValidatedOk() {
		Journalpost journalpost = TestDataGenerator.createJournalpostWithHoveddokument();
		journalpost.setJournalstatus(OD);
		journalpost.setJournalposttype(I);
		Long journalpostId = journalpostTestRepository.persist(journalpost).getJournalpostId();

		commitAndStartNewTransaction();

		var requestEntity = new HttpEntity<>(createHeadersWithServiceUserToken());
		ResponseEntity<String> response = restTemplate.exchange(URL_JOURNALPOST + journalpostId + FEILREGISTRER + SETT_STATUS_UTGAAR, PATCH, requestEntity, String.class);

		assertEquals(OK, response.getStatusCode());

		commitAndStartNewTransaction();

		Journalpost oppdatertJournalpost = journalpostTestRepository.findById(journalpostId).orElseThrow(RuntimeException::new);

		assertEquals(oppdatertJournalpost.getJournalstatus(), U);

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();

		assertEquals(1, aksjonsLoggList.size());

		AksjonsLogg aksjonsLogg = aksjonsLoggList.get(0);
		assertEquals(journalpostId, aksjonsLogg.getJournalpostId());
		assertEquals(SERVICE_USER_ID, aksjonsLogg.getUtfoertAv());
		assertEquals(UTGAAR, aksjonsLogg.getAksjon());
		assertEquals(HJEMMEL, aksjonsLogg.getHjemmel());
		assertEquals(1, aksjonsLogg.getArkivElementEndringer().size());
	}

	@Test
	public void shouldSetNavIdentInUtfoertAvWhenSaksbehandlerTokenSupplied() {
		Journalpost journalpost = TestDataGenerator.createJournalpostWithHoveddokument();
		Long journalpostId = journalpostTestRepository.persist(journalpost).getJournalpostId();

		commitAndStartNewTransaction();

		// feilregistrer
		HttpEntity<String> requestEntity = new HttpEntity<>(createHeadersWithUserAndServiceUserToken());
		ResponseEntity<String> feilregistrerResponse = restTemplate.exchange(URL_JOURNALPOST + journalpostId + FEILREGISTRER + FEILREGISTRER_SAKSTILKNYTNING, PATCH, requestEntity, String.class);
		assertEquals(OK, feilregistrerResponse.getStatusCode());

		// opphev feilregistrering
		ResponseEntity<String> opphevResponse = restTemplate.exchange(URL_JOURNALPOST + journalpostId + FEILREGISTRER + OPPHEV_FEILREGISTRERT_SAKSTILKNYTNING, PATCH, requestEntity, String.class);
		assertEquals(OK, opphevResponse.getStatusCode());

		commitAndStartNewTransaction();

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();

		assertThat(aksjonsLoggList).hasSize(2)
				.extracting(AksjonsLogg::getAksjon, AksjonsLogg::getUtfoertAv)
				.contains(tuple(AksjonsTypeCode.FEILREGISTRER_SAKSTILKNYTNING, PERSON_USER_ID),
						tuple(OPPHEV_FEILREGISTRERING, PERSON_USER_ID));
	}
}
