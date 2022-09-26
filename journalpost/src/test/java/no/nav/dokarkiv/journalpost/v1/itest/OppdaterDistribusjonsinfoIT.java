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
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

		OffsetDateTime readAtTimestamp = OffsetDateTime.now();
		OppdaterDistribusjonsinfoRequest oppdaterDistribusjonsinfoRequest = OppdaterDistribusjonsinfoRequest.builder()
				.utsendingsKanal(UtsendingsKanalCode.SDP.name())
				.settStatusEkspedert(true)
				.datoLest(readAtTimestamp)
				.build();
		var oppdaterDistribusjonsinfoEntity = new HttpEntity<>(oppdaterDistribusjonsinfoRequest, createHeadersWithServiceUserToken());

		ResponseEntity<String> response = restTemplate.exchange(URL_JOURNALPOST + journalpostId + "/oppdaterDistribusjonsinfo", HttpMethod.PATCH, oppdaterDistribusjonsinfoEntity, String.class);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		TestTransaction.start();
		Journalpost ferdigstiltJournalpost = joarkRepository.findById(journalpost.getJournalpostId()).orElseThrow(RuntimeException::new);

		assertEquals(JournalStatusCode.E, ferdigstiltJournalpost.getJournalstatus());
		assertEquals(UtsendingsKanalCode.SDP, ferdigstiltJournalpost.getUtsendingskanal());
		assertTrue(Duration.between(readAtTimestamp.toInstant(), ferdigstiltJournalpost.getLestDato().toInstant()).truncatedTo(ChronoUnit.SECONDS).isZero());

		TestTransaction.end();
	}

}