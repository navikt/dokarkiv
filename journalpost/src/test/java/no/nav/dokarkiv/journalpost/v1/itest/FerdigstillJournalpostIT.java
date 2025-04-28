package no.nav.dokarkiv.journalpost.v1.itest;

import no.nav.dokarkiv.core.datautil.JournalpostTestDataProvider;
import no.nav.dokarkiv.core.datautil.SakTestDataProvider;
import no.nav.dokarkiv.core.datautil.SaksrelasjonTestDataProvider;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.Sak;
import no.nav.dokarkiv.core.exceptions.ApplicationProblemDetail;
import no.nav.dokarkiv.journalpost.v1.api.FerdigstillJournalpostRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.time.LocalDateTime;
import java.util.List;

import static no.nav.dokarkiv.core.domain.codes.FagomradeCode.PEN;
import static no.nav.dokarkiv.core.domain.codes.FagomradeCode.RPO;
import static no.nav.dokarkiv.core.domain.codes.SakStatusCode.AAPEN;
import static no.nav.dokarkiv.core.domain.codes.SakStatusCode.AVSLUTTET;
import static no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode.L;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

public class FerdigstillJournalpostIT extends AbstractJournalpostIT {

	@Test
	public void happyPathInngaaende() {
		Sak sak = SakTestDataProvider.createSakWithStatus(AAPEN).build();
		sakTestRepository.persist(sak);
		Journalpost journalpost = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.M, sak.getSakId()).build();
		journalpostTestRepository.persist(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		Long journalpostId = journalpost.getJournalpostId();
		FerdigstillJournalpostRequest request = FerdigstillJournalpostRequest.builder()
				.journalfoerendeEnhet("9999")
				.build();

		var requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<String> response = restTemplate.exchange(apiJournalpostPath(journalpostId + FERDIGSTILL), PATCH, requestEntity, String.class);

		assertEquals(OK, response.getStatusCode());

		TestTransaction.start();
		Journalpost ferdigstiltJournalpost = journalpostTestRepository.findById(journalpost.getJournalpostId()).orElseThrow(RuntimeException::new);

		assertEquals(SERVICE_USER_ID, ferdigstiltJournalpost.getEndretAvNavn());
		assertEquals(SERVICE_USER_ID, ferdigstiltJournalpost.getEndretKildeNavn());
		assertEquals(SERVICE_USER_ID, ferdigstiltJournalpost.getJournalfortAvNavn());
		assertEquals(SERVICE_USER_ID, ferdigstiltJournalpost.getChangeStamp().getUpdatedBy());
		assertEquals(request.getJournalfoerendeEnhet(), ferdigstiltJournalpost.getJournalForendeEnhetId());
		assertEquals(JournalStatusCode.J, ferdigstiltJournalpost.getJournalstatus());
		assertThat(ferdigstiltJournalpost.getChangeStamp().getUpdatedDate()).isAfter(journalpost.getChangeStamp().getCreatedDate());
		assertEquals("Leonora Dorothea Dahl", ferdigstiltJournalpost.getOpprettetAvNavn());

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertEquals(1, aksjonsLoggList.size());
		assertEquals(SERVICE_USER_ID, aksjonsLoggList.get(0).getUtfoertAv());
		assertEquals(AksjonsTypeCode.FERDIGSTILL, aksjonsLoggList.get(0).getAksjon());
		assertEquals(3, aksjonsLoggList.get(0).getArkivElementEndringer().size());

		TestTransaction.end();
	}

	@Test // skal bli fjernet når migrering fra ondemand til Joark er ferdig, gjelder sak MMA-5695.
	public void happyPathInngaaendeForOndemand() {
		LocalDateTime datoJournal = LocalDateTime.now();
		LocalDateTime datoSendtPrint = LocalDateTime.now().minusDays(1);

		Sak sak = SakTestDataProvider.createSakWithStatus(AAPEN).build();
		sakTestRepository.persist(sak);
		Journalpost journalpost = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.M, sak.getSakId()).build();
		journalpostTestRepository.persist(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		Long journalpostId = journalpost.getJournalpostId();
		FerdigstillJournalpostRequest request = FerdigstillJournalpostRequest.builder()
				.journalfoerendeEnhet("9999")
				.journalfortAvNavn("journalfortAvNavn")
				.opprettetAvNavn("opprettetAvNavn")
				.datoJournal(datoJournal)
				.datoSendtPrint(datoSendtPrint)
				.build();

		var requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<String> response = restTemplate.exchange(apiJournalpostPath(journalpostId + FERDIGSTILL), PATCH, requestEntity, String.class);

		assertEquals(OK, response.getStatusCode());

		TestTransaction.start();
		Journalpost ferdigstiltJournalpost = journalpostTestRepository.findById(journalpost.getJournalpostId()).orElseThrow(RuntimeException::new);

		assertThat(ferdigstiltJournalpost.getJournalDato()).isEqualToIgnoringNanos(datoJournal);
		assertThat(ferdigstiltJournalpost.getSendtPrintDato()).isEqualToIgnoringNanos(datoSendtPrint);

		assertEquals(SERVICE_USER_ID, ferdigstiltJournalpost.getEndretAvNavn());
		assertEquals(SERVICE_USER_ID, ferdigstiltJournalpost.getEndretKildeNavn());
		assertEquals("journalfortAvNavn", ferdigstiltJournalpost.getJournalfortAvNavn());
		assertEquals(SERVICE_USER_ID, ferdigstiltJournalpost.getChangeStamp().getUpdatedBy());
		assertEquals(request.getJournalfoerendeEnhet(), ferdigstiltJournalpost.getJournalForendeEnhetId());
		assertEquals(JournalStatusCode.J, ferdigstiltJournalpost.getJournalstatus());
		assertThat(ferdigstiltJournalpost.getChangeStamp().getUpdatedDate()).isAfter(journalpost.getChangeStamp().getCreatedDate());
		assertEquals("opprettetAvNavn", ferdigstiltJournalpost.getOpprettetAvNavn());

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertEquals(1, aksjonsLoggList.size());
		assertEquals(SERVICE_USER_ID, aksjonsLoggList.get(0).getUtfoertAv());
		assertEquals(AksjonsTypeCode.FERDIGSTILL, aksjonsLoggList.get(0).getAksjon());
		assertEquals(3, aksjonsLoggList.get(0).getArkivElementEndringer().size());

		TestTransaction.end();
	}

	@Test
	public void happyPathUtgaaende() {
		Sak sak = SakTestDataProvider.createSakWithStatus(AAPEN).build();
		sakTestRepository.persist(sak);
		Journalpost journalpost = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.U, JournalStatusCode.M, sak.getSakId()).build();
		journalpostTestRepository.persist(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		Long journalpostId = journalpost.getJournalpostId();
		FerdigstillJournalpostRequest request = FerdigstillJournalpostRequest.builder()
				.journalfoerendeEnhet("9999")
				.build();

		var requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<String> response = restTemplate.exchange(apiJournalpostPath(journalpostId + FERDIGSTILL), PATCH, requestEntity, String.class);

		assertEquals(OK, response.getStatusCode());

		TestTransaction.start();
		Journalpost ferdigstiltJournalpost = journalpostTestRepository.findById(journalpost.getJournalpostId()).orElseThrow(RuntimeException::new);

		assertEquals(SERVICE_USER_ID, ferdigstiltJournalpost.getEndretAvNavn());
		assertEquals(SERVICE_USER_ID, ferdigstiltJournalpost.getEndretKildeNavn());
		assertEquals(SERVICE_USER_ID, ferdigstiltJournalpost.getJournalfortAvNavn());
		assertEquals(SERVICE_USER_ID, ferdigstiltJournalpost.getChangeStamp().getUpdatedBy());
		assertEquals(request.getJournalfoerendeEnhet(), ferdigstiltJournalpost.getJournalForendeEnhetId());
		assertEquals(JournalStatusCode.FS, ferdigstiltJournalpost.getJournalstatus());
		assertThat(ferdigstiltJournalpost.getChangeStamp().getUpdatedDate()).isAfter(journalpost.getChangeStamp().getCreatedDate());
		TestTransaction.end();
	}

	@ParameterizedTest
	@EnumSource(value = JournalStatusCode.class, names = {"U", "UB", "FS", "R", "FL", "A"} )
	public void happyPathUtgaaendeJournalstatusKanFerdigstilles(JournalStatusCode journalstatus) {
		Sak sak = SakTestDataProvider.createSakWithStatus(AAPEN).build();
		sakTestRepository.persist(sak);
		Journalpost journalpost = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.U, journalstatus, sak.getSakId()).build();
		journalpostTestRepository.persist(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		Long journalpostId = journalpost.getJournalpostId();
		FerdigstillJournalpostRequest request = FerdigstillJournalpostRequest.builder()
				.journalfoerendeEnhet("9999")
				.build();

		var requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<String> response = restTemplate.exchange(apiJournalpostPath(journalpostId + FERDIGSTILL), PATCH, requestEntity, String.class);

		assertEquals(OK, response.getStatusCode());

		TestTransaction.start();
		Journalpost ferdigstiltJournalpost = journalpostTestRepository.findById(journalpost.getJournalpostId()).orElseThrow(RuntimeException::new);

		assertEquals(SERVICE_USER_ID, ferdigstiltJournalpost.getEndretAvNavn());
		assertEquals(SERVICE_USER_ID, ferdigstiltJournalpost.getEndretKildeNavn());
		assertEquals(SERVICE_USER_ID, ferdigstiltJournalpost.getJournalfortAvNavn());
		assertEquals(SERVICE_USER_ID, ferdigstiltJournalpost.getChangeStamp().getUpdatedBy());
		assertEquals(request.getJournalfoerendeEnhet(), ferdigstiltJournalpost.getJournalForendeEnhetId());
		assertEquals(JournalStatusCode.FS, ferdigstiltJournalpost.getJournalstatus());
		assertThat(ferdigstiltJournalpost.getChangeStamp().getUpdatedDate()).isAfter(journalpost.getChangeStamp().getCreatedDate());
		TestTransaction.end();
	}

	@Test
	public void happyPathUtgaaendeUtsendingsKanalL() {
		Sak sak = SakTestDataProvider.createSakWithStatus(AAPEN).build();
		sakTestRepository.persist(sak);
		Journalpost journalpost = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.U, JournalStatusCode.M, sak.getSakId()).build();
		journalpost.setUtsendingskanal(L);
		journalpostTestRepository.persist(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		Long journalpostId = journalpost.getJournalpostId();
		FerdigstillJournalpostRequest request = FerdigstillJournalpostRequest.builder()
				.journalfoerendeEnhet("9999")
				.build();

		var requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<String> response = restTemplate.exchange(apiJournalpostPath(journalpostId + FERDIGSTILL), PATCH, requestEntity, String.class);

		assertEquals(OK, response.getStatusCode());

		TestTransaction.start();
		Journalpost ferdigstiltJournalpost = journalpostTestRepository.findById(journalpost.getJournalpostId()).orElseThrow(RuntimeException::new);

		assertEquals(SERVICE_USER_ID, ferdigstiltJournalpost.getEndretAvNavn());
		assertEquals(SERVICE_USER_ID, ferdigstiltJournalpost.getEndretKildeNavn());
		assertEquals(SERVICE_USER_ID, ferdigstiltJournalpost.getJournalfortAvNavn());
		assertEquals(SERVICE_USER_ID, ferdigstiltJournalpost.getChangeStamp().getUpdatedBy());
		assertEquals(request.getJournalfoerendeEnhet(), ferdigstiltJournalpost.getJournalForendeEnhetId());
		assertEquals(JournalStatusCode.FL, ferdigstiltJournalpost.getJournalstatus());
		assertThat(ferdigstiltJournalpost.getChangeStamp().getUpdatedDate()).isAfter(journalpost.getChangeStamp().getCreatedDate());
		TestTransaction.end();
	}

	@Test
	public void happyPathNotat() {
		Sak sak = SakTestDataProvider.createSakWithStatus(AAPEN).build();
		sakTestRepository.persist(sak);
		Journalpost journalpost = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.N, JournalStatusCode.M, sak.getSakId()).build();
		journalpost.setAvsenderMottaker(null);
		journalpostTestRepository.persist(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		Long journalpostId = journalpost.getJournalpostId();
		FerdigstillJournalpostRequest request = FerdigstillJournalpostRequest.builder()
				.journalfoerendeEnhet("9999")
				.build();

		var requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<String> response = restTemplate.exchange(apiJournalpostPath(journalpostId + FERDIGSTILL), PATCH, requestEntity, String.class);

		assertEquals(OK, response.getStatusCode());

		TestTransaction.start();
		Journalpost ferdigstiltJournalpost = journalpostTestRepository.findById(journalpost.getJournalpostId()).orElseThrow(RuntimeException::new);

		assertEquals(SERVICE_USER_ID, ferdigstiltJournalpost.getEndretAvNavn());
		assertEquals(SERVICE_USER_ID, ferdigstiltJournalpost.getEndretKildeNavn());
		assertEquals(SERVICE_USER_ID, ferdigstiltJournalpost.getJournalfortAvNavn());
		assertEquals(SERVICE_USER_ID, ferdigstiltJournalpost.getChangeStamp().getUpdatedBy());
		assertEquals(request.getJournalfoerendeEnhet(), ferdigstiltJournalpost.getJournalForendeEnhetId());
		assertEquals(JournalStatusCode.FL, ferdigstiltJournalpost.getJournalstatus());
		assertThat(ferdigstiltJournalpost.getChangeStamp().getUpdatedDate()).isAfter(journalpost.getChangeStamp().getCreatedDate());
		TestTransaction.end();
	}

	@Test
	public void shouldFailIfJournalpostIsNotMidlertidig() {
		Journalpost journalpost = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.U, JournalStatusCode.E).build();
		journalpost.getSaksrelasjon().setFeilregistrert(true);
		journalpostTestRepository.persist(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		Long journalpostId = journalpost.getJournalpostId();
		FerdigstillJournalpostRequest request = FerdigstillJournalpostRequest.builder()
				.journalfoerendeEnhet("9999")
				.build();

		var requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<ApplicationProblemDetail> response =
				restTemplate.exchange(apiJournalpostPath(journalpostId + FERDIGSTILL), PATCH,
						requestEntity, ApplicationProblemDetail.class);
		assertEquals(BAD_REQUEST, response.getStatusCode());
		assertNotNull(response.getBody());
		assertNotNull(response.getBody().getMessage());

		assertThat(response.getBody().getMessage()).contains(
				String.format("Kunne ikke ferdigstille journalpost med journalpostId=%s. Journalposten er ikke ansett som midlertidig journalført av følgende grunn(er):", journalpostId),
				String.format("Den har journalstatus=%s", journalpost.getJournalstatus()),
				"Den er feilregistrert"
		);
	}

	@Test
	public void shouldFailIfRequestJournalfoerendeEnhetIsInvalid() {
		Journalpost journalpost = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.U, JournalStatusCode.FS).build();
		journalpostTestRepository.persist(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		Long journalpostId = journalpost.getJournalpostId();
		FerdigstillJournalpostRequest request = FerdigstillJournalpostRequest.builder()
				.journalfoerendeEnhet("abc")
				.build();

		var requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<String> response = restTemplate.exchange(apiJournalpostPath(journalpostId + FERDIGSTILL), PATCH, requestEntity, String.class);

		assertEquals(BAD_REQUEST, response.getStatusCode());
		assertThat(response.getBody().contains("Feltet journalfoerendeEnhet må ha lengde=4, men har lengde=3. journalfoerendeEnhet=abc"));
	}

	@Test
	public void shouldFailIfRequestJournalpostIdIsInvalid() {
		Journalpost journalpost = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.U, JournalStatusCode.FS).build();
		journalpostTestRepository.persist(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		String journalpostId = "abc";
		FerdigstillJournalpostRequest request = FerdigstillJournalpostRequest.builder()
				.journalfoerendeEnhet("9999")
				.build();

		var requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<String> response =
				restTemplate.exchange(apiJournalpostPath(journalpostId + FERDIGSTILL), PATCH,
						requestEntity, String.class);

		assertEquals(BAD_REQUEST, response.getStatusCode());
	}

	@Test
	public void shouldFailIfMissingPaakrevdFelter() {
		Journalpost journalpost = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.M).build();
		journalpost.setAvsenderMottaker(null);
		journalpost.setInnhold(null);
		journalpostTestRepository.persist(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		Long journalpostId = journalpost.getJournalpostId();
		FerdigstillJournalpostRequest request = FerdigstillJournalpostRequest.builder()
				.journalfoerendeEnhet("9999")
				.build();

		var requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<ApplicationProblemDetail> response =
				restTemplate.exchange(apiJournalpostPath(journalpostId + FERDIGSTILL), PATCH,
						requestEntity, ApplicationProblemDetail.class);

		assertEquals(BAD_REQUEST, response.getStatusCode());
		assertNotNull(response.getBody());
		assertTrue(response.getBody().getMessage().contains("avsenderMottaker.navn"));
		assertTrue(response.getBody().getMessage().contains("tittel"));
	}

	@Test
	public void shouldFailIfFagomradeErInaktivt() {
		Journalpost journalpost = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.M).build();
		String inaktivtFagomrade = "UKJ";
		journalpost.setFagomrade(FagomradeCode.valueOf(inaktivtFagomrade));
		journalpostTestRepository.persist(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		Long journalpostId = journalpost.getJournalpostId();
		FerdigstillJournalpostRequest request = FerdigstillJournalpostRequest.builder()
				.journalfoerendeEnhet("9999")
				.build();

		var requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<ApplicationProblemDetail> response =
				restTemplate.exchange(apiJournalpostPath(journalpostId + FERDIGSTILL), PATCH,
						requestEntity, ApplicationProblemDetail.class);

		assertEquals(BAD_REQUEST, response.getStatusCode());
		assertNotNull(response.getBody());
		assertTrue(response.getBody().getMessage().contains(String.format("Tema=%s på journalposten er ikke gyldig for ferdigstilling. " +
																		  "For å unngå dette i fremtiden bør du fjerne muligheten til å ferdigstille på ugyldige tema", inaktivtFagomrade)));
	}

	@Test
	public void shouldSetNavUserIdHeaderSporingWhenServiceUserTokenAndNavUserIdHeaderIsSet() {
		stubMsGraphGetUser(NAV_IDENT_SAKSBEHANDLER);

		Sak sak = SakTestDataProvider.createSakWithStatus(AAPEN).build();
		sakTestRepository.persist(sak);
		Journalpost journalpost = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.U, JournalStatusCode.M, sak.getSakId()).build();
		journalpostTestRepository.persist(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		Long journalpostId = journalpost.getJournalpostId();
		FerdigstillJournalpostRequest request = FerdigstillJournalpostRequest.builder()
				.journalfoerendeEnhet("9999")
				.build();

		var requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserTokenAndUserIdHeader(SERVICE_USER_ID, NAV_USER_ID));
		ResponseEntity<String> response = restTemplate.exchange(apiJournalpostPath(journalpostId + FERDIGSTILL), PATCH, requestEntity, String.class);

		assertEquals(OK, response.getStatusCode());

		TestTransaction.start();
		Journalpost ferdigstiltJournalpost = journalpostTestRepository.findById(journalpost.getJournalpostId()).orElseThrow(RuntimeException::new);

		assertEquals(PERSON_USER_NAME, ferdigstiltJournalpost.getEndretAvNavn());
		assertEquals(SERVICE_USER_ID, ferdigstiltJournalpost.getEndretKildeNavn());
		assertEquals(PERSON_USER_NAME, ferdigstiltJournalpost.getJournalfortAvNavn());
		assertEquals(NAV_USER_ID, ferdigstiltJournalpost.getChangeStamp().getUpdatedBy());
		TestTransaction.end();
	}

	@Test
	public void shouldFailWhenSakIsAvsluttetAndJournalpostIsBeingFerdigstilt() {
		Sak sak = SakTestDataProvider.createSakWithStatus(AVSLUTTET).build();
		sakTestRepository.persist(sak);
		Journalpost journalpost = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.FS, sak.getSakId())
				.saksrelasjon(SaksrelasjonTestDataProvider.createSaksrelasjonWithSak(sak.getSakId()).build())
				.build();
		journalpostTestRepository.persist(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		Long journalpostId = journalpost.getJournalpostId();
		FerdigstillJournalpostRequest request = FerdigstillJournalpostRequest.builder()
				.journalfoerendeEnhet("9999")
				.build();

		var requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<ApplicationProblemDetail> response =
				restTemplate.exchange(apiJournalpostPath(journalpostId + FERDIGSTILL), PATCH,
						requestEntity, ApplicationProblemDetail.class);


		assertEquals(BAD_REQUEST, response.getStatusCode());
		assertNotNull(response.getBody());
		assertThat(response.getBody().getMessage()).contains("Journalposten kan ikke ferdigstilles som generell sak eller fagsak med sakstatus=%s. Sakstatus må være=AAPEN eller null".formatted(AVSLUTTET));
	}

	@Test
	public void happyPathWhenSakStatusIsNull() {
		Sak sak = SakTestDataProvider.createSakWithStatus(null).tema(PEN.name()).build();
		sakTestRepository.persist(sak);
		Journalpost journalpost = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.FS, sak.getSakId())
				.saksrelasjon(SaksrelasjonTestDataProvider.createSaksrelasjonWithSak(sak.getSakId()).build())
				.build();
		journalpostTestRepository.persist(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		Long journalpostId = journalpost.getJournalpostId();
		FerdigstillJournalpostRequest request = FerdigstillJournalpostRequest.builder()
				.journalfoerendeEnhet("9999")
				.build();

		var requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<String> response =
				restTemplate.exchange(apiJournalpostPath(journalpostId.toString(), FERDIGSTILL), PATCH, requestEntity, String.class);

		assertEquals(OK, response.getStatusCode());
	}

	@Test
	public void shouldFailWhenSakTemaErInaktivt() {
		Sak sak = SakTestDataProvider.createSakWithStatus(null).tema(RPO.name()).build();
		sakTestRepository.persist(sak);

		Journalpost journalpost = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.FS, sak.getSakId())
				.saksrelasjon(SaksrelasjonTestDataProvider.createSaksrelasjonWithSak(sak.getSakId()).build())
				.build();
		journalpostTestRepository.persist(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		Long journalpostId = journalpost.getJournalpostId();
		FerdigstillJournalpostRequest request = FerdigstillJournalpostRequest.builder()
				.journalfoerendeEnhet("9999")
				.build();

		var requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<String> response =
				restTemplate.exchange(apiJournalpostPath(journalpostId + FERDIGSTILL), PATCH,
						requestEntity, String.class);

		assertEquals(BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody().contains("Sakens tema=RPO er ugyldig og journalposten kan ikke ferdigstilles"));
	}
}