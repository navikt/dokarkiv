package no.nav.dokarkiv.ferdigstilljournalpost.v1.itest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import no.nav.dokarkiv.core.datautil.JournalpostTestDataProvider;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.ferdigstilljournalpost.v1.api.FerdigstillJournalpostRequest;
import org.apache.commons.collections15.IteratorUtils;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.io.IOException;
import java.util.List;

public class FerdigstillJournalpostIT extends AbstractFerdigstillJournalpostIT {

	@Test
	public void happyPathInngaaende() throws IOException {
		abacPermit();

		Journalpost journalpost = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.M).build();
		joarkRepository.save(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		FerdigstillJournalpostRequest request = FerdigstillJournalpostRequest.builder()
				.journalpostId(journalpost.getJournalpostId().toString())
				.journalfEnhet("9999")
				.build();

		HttpEntity requestEntity = new HttpEntity(request, createHeadersWithServiceUserToken());
		ResponseEntity<String> response = restTemplate.exchange(URL_FERDIGSTILLJOURNALPOST, HttpMethod.PATCH, requestEntity, String.class);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		TestTransaction.start();
		Journalpost ferdigstiltJournalpost = joarkRepository.findById(journalpost.getJournalpostId()).orElseThrow(RuntimeException::new);

		assertEquals(SERVICE_USER_ID, ferdigstiltJournalpost.getEndretAvNavn());
		assertEquals(SERVICE_USER_ID, ferdigstiltJournalpost.getEndretKildeNavn());
		assertEquals(SERVICE_USER_ID, ferdigstiltJournalpost.getJournalfortAvNavn());
		assertEquals(SERVICE_USER_ID, ferdigstiltJournalpost.getChangeStamp().getUpdatedBy());
		assertEquals(request.getJournalfEnhet(), ferdigstiltJournalpost.getJournalForendeEnhetId());
		assertEquals(JournalStatusCode.J, ferdigstiltJournalpost.getJournalstatus());
		assertTrue(ferdigstiltJournalpost.getChangeStamp().getUpdatedDate().after(journalpost.getChangeStamp().getCreatedDate()));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertEquals(1, aksjonsLoggList.size());
		assertEquals(SERVICE_USER_ID, aksjonsLoggList.get(0).getUtfoertAv());
		assertEquals(AksjonsTypeCode.FERDIGSTILL, aksjonsLoggList.get(0).getAksjon());
		assertEquals(4, aksjonsLoggList.get(0).getArkivElementEndringer().size());

		TestTransaction.end();
	}

	@Test
	public void happyPathUtgaaende() throws IOException {
		abacPermit();

		Journalpost journalpost = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.U, JournalStatusCode.M).build();
		joarkRepository.save(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		FerdigstillJournalpostRequest request = FerdigstillJournalpostRequest.builder()
				.journalpostId(journalpost.getJournalpostId().toString())
				.journalfEnhet("9999")
				.build();

		HttpEntity requestEntity = new HttpEntity(request, createHeadersWithServiceUserToken());
		ResponseEntity<String> response = restTemplate.exchange(URL_FERDIGSTILLJOURNALPOST, HttpMethod.PATCH, requestEntity, String.class);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		TestTransaction.start();
		Journalpost ferdigstiltJournalpost = joarkRepository.findById(journalpost.getJournalpostId()).orElseThrow(RuntimeException::new);

		assertEquals(SERVICE_USER_ID, ferdigstiltJournalpost.getEndretAvNavn());
		assertEquals(SERVICE_USER_ID, ferdigstiltJournalpost.getEndretKildeNavn());
		assertEquals(SERVICE_USER_ID, ferdigstiltJournalpost.getJournalfortAvNavn());
		assertEquals(SERVICE_USER_ID, ferdigstiltJournalpost.getChangeStamp().getUpdatedBy());
		assertEquals(request.getJournalfEnhet(), ferdigstiltJournalpost.getJournalForendeEnhetId());
		assertEquals(JournalStatusCode.FS, ferdigstiltJournalpost.getJournalstatus());
		assertTrue(ferdigstiltJournalpost.getChangeStamp().getUpdatedDate().after(journalpost.getChangeStamp().getCreatedDate()));
		TestTransaction.end();
	}

	@Test
	public void happyPathNotat() throws IOException {
		abacPermit();

		Journalpost journalpost = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.N, JournalStatusCode.M).build();
		joarkRepository.save(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		FerdigstillJournalpostRequest request = FerdigstillJournalpostRequest.builder()
				.journalpostId(journalpost.getJournalpostId().toString())
				.journalfEnhet("9999")
				.build();

		HttpEntity requestEntity = new HttpEntity(request, createHeadersWithServiceUserToken());
		ResponseEntity<String> response = restTemplate.exchange(URL_FERDIGSTILLJOURNALPOST, HttpMethod.PATCH, requestEntity, String.class);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		TestTransaction.start();
		Journalpost ferdigstiltJournalpost = joarkRepository.findById(journalpost.getJournalpostId()).orElseThrow(RuntimeException::new);

		assertEquals(SERVICE_USER_ID, ferdigstiltJournalpost.getEndretAvNavn());
		assertEquals(SERVICE_USER_ID, ferdigstiltJournalpost.getEndretKildeNavn());
		assertEquals(SERVICE_USER_ID, ferdigstiltJournalpost.getJournalfortAvNavn());
		assertEquals(SERVICE_USER_ID, ferdigstiltJournalpost.getChangeStamp().getUpdatedBy());
		assertEquals(request.getJournalfEnhet(), ferdigstiltJournalpost.getJournalForendeEnhetId());
		assertEquals(JournalStatusCode.FS, ferdigstiltJournalpost.getJournalstatus());
		assertTrue(ferdigstiltJournalpost.getChangeStamp().getUpdatedDate().after(journalpost.getChangeStamp().getCreatedDate()));
		TestTransaction.end();
	}

	@Test
	public void shouldFailIfJournalpostIsNotMidlertidig() throws IOException {
		abacPermit();

		Journalpost journalpost = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.U, JournalStatusCode.FS).build();
		joarkRepository.save(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		FerdigstillJournalpostRequest request = FerdigstillJournalpostRequest.builder()
				.journalpostId(journalpost.getJournalpostId().toString())
				.journalfEnhet("9999")
				.build();

		HttpEntity requestEntity = new HttpEntity(request, createHeadersWithServiceUserToken());
		ResponseEntity<String> response = restTemplate.exchange(URL_FERDIGSTILLJOURNALPOST, HttpMethod.PATCH, requestEntity, String.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}

	@Test
	public void shouldFailIfRequestJournalfEnhetIsInvalid() throws IOException {
		abacPermit();

		Journalpost journalpost = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.U, JournalStatusCode.FS).build();
		joarkRepository.save(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		FerdigstillJournalpostRequest request = FerdigstillJournalpostRequest.builder()
				.journalpostId(journalpost.getJournalpostId().toString())
				.journalfEnhet("abc")
				.build();

		HttpEntity requestEntity = new HttpEntity(request, createHeadersWithServiceUserToken());
		ResponseEntity<String> response = restTemplate.exchange(URL_FERDIGSTILLJOURNALPOST, HttpMethod.PATCH, requestEntity, String.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}

	@Test
	public void shouldFailIfRequestJournalpostIdIsInvalid() throws IOException {
		abacPermit();

		Journalpost journalpost = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.U, JournalStatusCode.FS).build();
		joarkRepository.save(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		FerdigstillJournalpostRequest request = FerdigstillJournalpostRequest.builder()
				.journalpostId("abc")
				.journalfEnhet("9999")
				.build();

		HttpEntity requestEntity = new HttpEntity(request, createHeadersWithServiceUserToken());
		ResponseEntity<String> response = restTemplate.exchange(URL_FERDIGSTILLJOURNALPOST, HttpMethod.PATCH, requestEntity, String.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}
}