package no.nav.dokarkiv.journalpost.v1.itest;

import no.nav.dokarkiv.core.datautil.JournalpostTestDataProvider;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.journalpost.v1.api.FerdigstillJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterDistribusjonsinfoRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OppdaterDistribusjonsinfoIT extends AbstractJournalpostIT {

	@Test
	public void happyPathUpdateDistribusjonsinfo() throws IOException {
		abacPermit();

		Journalpost journalpost = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.U, JournalStatusCode.M).build();
		joarkRepository.save(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		Long journalpostId = journalpost.getJournalpostId();
		FerdigstillJournalpostRequest request = FerdigstillJournalpostRequest.builder()
				.journalfoerendeEnhet("9999")
				.build();

		var finalizeRequestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<String> finalizeResponse = restTemplate.exchange(URL_JOURNALPOST + journalpostId + FERDIGSTILL, HttpMethod.PATCH, finalizeRequestEntity, String.class);

		assertEquals(HttpStatus.OK, finalizeResponse.getStatusCode());

		OppdaterDistribusjonsinfoRequest oppdaterDistribusjonsinfoRequest = OppdaterDistribusjonsinfoRequest.builder()
				.utsendingsKanal(UtsendingsKanalCode.SDP.name())
				.settStatusEkspedert(true)
				.build();
		var oppdaterDistribusjonsinfoEntity = new HttpEntity<>(oppdaterDistribusjonsinfoRequest, createHeadersWithServiceUserToken());

		ResponseEntity<String> response = restTemplate.exchange(URL_JOURNALPOST + journalpostId + "/oppdaterDistribusjonsinfo", HttpMethod.PATCH, oppdaterDistribusjonsinfoEntity, String.class);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		TestTransaction.start();
		Journalpost ferdigstiltJournalpost = joarkRepository.findById(journalpost.getJournalpostId()).orElseThrow(RuntimeException::new);

		assertEquals(JournalStatusCode.E, ferdigstiltJournalpost.getJournalstatus());
		assertEquals(UtsendingsKanalCode.SDP, ferdigstiltJournalpost.getUtsendingskanal());
		assertNull(ferdigstiltJournalpost.getLestDato());

		TestTransaction.end();
	}

	@Test
	public void happyPathUpdateDistribusjonsinfoSettLestDato() throws IOException {
		abacPermit();
		var clock = Clock.fixed(Instant.now().minus(1, ChronoUnit.HOURS), ZoneId.systemDefault());

		Journalpost journalpost = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.U, JournalStatusCode.M).build();
		joarkRepository.save(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		Long journalpostId = journalpost.getJournalpostId();
		FerdigstillJournalpostRequest request = FerdigstillJournalpostRequest.builder()
				.journalfoerendeEnhet("9999")
				.build();

		var finalizeRequestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<String> finalizeResponse = restTemplate.exchange(URL_JOURNALPOST + journalpostId + FERDIGSTILL, HttpMethod.PATCH, finalizeRequestEntity, String.class);

		assertEquals(HttpStatus.OK, finalizeResponse.getStatusCode());

		OffsetDateTime firstReadAtTimestamp = OffsetDateTime.now(clock);
		OppdaterDistribusjonsinfoRequest firstOppdaterDistribusjonsinfoRequest = OppdaterDistribusjonsinfoRequest.builder()
				.utsendingsKanal(UtsendingsKanalCode.SDP.name())
				.settStatusEkspedert(false)
				.datoLest(firstReadAtTimestamp)
				.build();
		var firstOppdaterDistribusjonsinfoEntity = new HttpEntity<>(firstOppdaterDistribusjonsinfoRequest, createHeadersWithServiceUserToken());

		ResponseEntity<String> response1 = restTemplate.exchange(URL_JOURNALPOST + journalpostId + "/oppdaterDistribusjonsinfo", HttpMethod.PATCH, firstOppdaterDistribusjonsinfoEntity, String.class);

		assertEquals(HttpStatus.OK, response1.getStatusCode());

		clock = Clock.fixed(Instant.now().plus(1, ChronoUnit.HOURS), ZoneId.systemDefault());
		OffsetDateTime secondReadAtTimestamp = OffsetDateTime.now(clock);
		OppdaterDistribusjonsinfoRequest secondOppdaterDistribusjonsinfoRequest = OppdaterDistribusjonsinfoRequest.builder()
				.utsendingsKanal(UtsendingsKanalCode.SDP.name())
				.settStatusEkspedert(true)
				.datoLest(secondReadAtTimestamp)
				.build();
		var secondOppdaterDistribusjonsinfoEntity = new HttpEntity<>(secondOppdaterDistribusjonsinfoRequest, createHeadersWithServiceUserToken());

		ResponseEntity<String> response2 = restTemplate.exchange(URL_JOURNALPOST + journalpostId + "/oppdaterDistribusjonsinfo", HttpMethod.PATCH, secondOppdaterDistribusjonsinfoEntity, String.class);
		assertEquals(HttpStatus.OK, response2.getStatusCode());

		TestTransaction.start();
		Journalpost ferdigstiltJournalpost = joarkRepository.findById(journalpost.getJournalpostId()).orElseThrow(RuntimeException::new);

		assertEquals(JournalStatusCode.E, ferdigstiltJournalpost.getJournalstatus());
		assertEquals(UtsendingsKanalCode.SDP, ferdigstiltJournalpost.getUtsendingskanal());
		assertTrue(Duration.between(firstReadAtTimestamp.toInstant(), ferdigstiltJournalpost.getLestDato().toInstant()).truncatedTo(ChronoUnit.SECONDS).isZero());

		TestTransaction.end();
	}

}