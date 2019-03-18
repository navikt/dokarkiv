package no.nav.dokarkiv.journalpost.v1.itest;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.consumer.RestConsumerExceptionResponse;
import no.nav.dokarkiv.core.datautil.JournalpostTestDataProvider;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.journalpost.v1.api.FerdigstillJournalpostRequest;
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

		Long journalpostId = journalpost.getJournalpostId();
		FerdigstillJournalpostRequest request = FerdigstillJournalpostRequest.builder()
				.journalfoerendeEnhet("9999")
				.build();

		HttpEntity requestEntity = new HttpEntity(request, createHeadersWithServiceUserToken());
		ResponseEntity<String> response = restTemplate.exchange(URL_FERDIGSTILLJOURNALPOST + journalpostId + FERDIGSTILL, HttpMethod.PATCH, requestEntity, String.class);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		TestTransaction.start();
		Journalpost ferdigstiltJournalpost = joarkRepository.findById(journalpost.getJournalpostId()).orElseThrow(RuntimeException::new);

		assertEquals(SERVICE_USER_ID, ferdigstiltJournalpost.getEndretAvNavn());
		assertEquals(SERVICE_USER_ID, ferdigstiltJournalpost.getEndretKildeNavn());
		assertEquals(SERVICE_USER_ID, ferdigstiltJournalpost.getJournalfortAvNavn());
		assertEquals(SERVICE_USER_ID, ferdigstiltJournalpost.getChangeStamp().getUpdatedBy());
		assertEquals(request.getJournalfoerendeEnhet(), ferdigstiltJournalpost.getJournalForendeEnhetId());
		assertEquals(JournalStatusCode.J, ferdigstiltJournalpost.getJournalstatus());
		assertTrue(ferdigstiltJournalpost.getChangeStamp().getUpdatedDate().after(journalpost.getChangeStamp().getCreatedDate()));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertEquals(1, aksjonsLoggList.size());
		assertEquals(SERVICE_USER_ID, aksjonsLoggList.get(0).getUtfoertAv());
		assertEquals(AksjonsTypeCode.FERDIGSTILL, aksjonsLoggList.get(0).getAksjon());
		assertEquals(3, aksjonsLoggList.get(0).getArkivElementEndringer().size());

		TestTransaction.end();
	}

	@Test
	public void happyPathUtgaaende() throws IOException {
		abacPermit();

		Journalpost journalpost = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.U, JournalStatusCode.M).build();
		joarkRepository.save(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		Long journalpostId = journalpost.getJournalpostId();
		FerdigstillJournalpostRequest request = FerdigstillJournalpostRequest.builder()
				.journalfoerendeEnhet("9999")
				.build();

		HttpEntity requestEntity = new HttpEntity(request, createHeadersWithServiceUserToken());
		ResponseEntity<String> response = restTemplate.exchange(URL_FERDIGSTILLJOURNALPOST + journalpostId + FERDIGSTILL, HttpMethod.PATCH, requestEntity, String.class);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		TestTransaction.start();
		Journalpost ferdigstiltJournalpost = joarkRepository.findById(journalpost.getJournalpostId()).orElseThrow(RuntimeException::new);

		assertEquals(SERVICE_USER_ID, ferdigstiltJournalpost.getEndretAvNavn());
		assertEquals(SERVICE_USER_ID, ferdigstiltJournalpost.getEndretKildeNavn());
		assertEquals(SERVICE_USER_ID, ferdigstiltJournalpost.getJournalfortAvNavn());
		assertEquals(SERVICE_USER_ID, ferdigstiltJournalpost.getChangeStamp().getUpdatedBy());
		assertEquals(request.getJournalfoerendeEnhet(), ferdigstiltJournalpost.getJournalForendeEnhetId());
		assertEquals(JournalStatusCode.FS, ferdigstiltJournalpost.getJournalstatus());
		assertTrue(ferdigstiltJournalpost.getChangeStamp().getUpdatedDate().after(journalpost.getChangeStamp().getCreatedDate()));
		TestTransaction.end();
	}

	@Test
	public void happyPathNotat() throws IOException {
		abacPermit();

		Journalpost journalpost = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.N, JournalStatusCode.M).build();
		journalpost.setAvsenderMottaker(null);
		joarkRepository.save(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		Long journalpostId = journalpost.getJournalpostId();
		FerdigstillJournalpostRequest request = FerdigstillJournalpostRequest.builder()
				.journalfoerendeEnhet("9999")
				.build();

		HttpEntity requestEntity = new HttpEntity(request, createHeadersWithServiceUserToken());
		ResponseEntity<String> response = restTemplate.exchange(URL_FERDIGSTILLJOURNALPOST + journalpostId + FERDIGSTILL, HttpMethod.PATCH, requestEntity, String.class);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		TestTransaction.start();
		Journalpost ferdigstiltJournalpost = joarkRepository.findById(journalpost.getJournalpostId()).orElseThrow(RuntimeException::new);

		assertEquals(SERVICE_USER_ID, ferdigstiltJournalpost.getEndretAvNavn());
		assertEquals(SERVICE_USER_ID, ferdigstiltJournalpost.getEndretKildeNavn());
		assertEquals(SERVICE_USER_ID, ferdigstiltJournalpost.getJournalfortAvNavn());
		assertEquals(SERVICE_USER_ID, ferdigstiltJournalpost.getChangeStamp().getUpdatedBy());
		assertEquals(request.getJournalfoerendeEnhet(), ferdigstiltJournalpost.getJournalForendeEnhetId());
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

		Long journalpostId = journalpost.getJournalpostId();
		FerdigstillJournalpostRequest request = FerdigstillJournalpostRequest.builder()
				.journalfoerendeEnhet("9999")
				.build();

		HttpEntity requestEntity = new HttpEntity(request, createHeadersWithServiceUserToken());
		ResponseEntity<RestConsumerExceptionResponse> response =
				restTemplate.exchange(URL_FERDIGSTILLJOURNALPOST + journalpostId + FERDIGSTILL, HttpMethod.PATCH,
						requestEntity, RestConsumerExceptionResponse.class);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertNotNull(response.getBody().getMessage());
		assertEquals(String.format("Journalpost med journalpostId=%s er ikke midlertidig journalført", journalpostId), response.getBody().getMessage());
	}

	@Test
	public void shouldFailIfRequestJournalfoerendeEnhetIsInvalid() throws IOException {
		abacPermit();

		Journalpost journalpost = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.U, JournalStatusCode.FS).build();
		joarkRepository.save(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		Long journalpostId = journalpost.getJournalpostId();
		FerdigstillJournalpostRequest request = FerdigstillJournalpostRequest.builder()
				.journalfoerendeEnhet("abc")
				.build();

		HttpEntity requestEntity = new HttpEntity(request, createHeadersWithServiceUserToken());
		ResponseEntity<String> response = restTemplate.exchange(URL_FERDIGSTILLJOURNALPOST + journalpostId + FERDIGSTILL, HttpMethod.PATCH, requestEntity, String.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}

	@Test
	public void shouldFailIfJournalfEnhetInRequestIsInvalid() throws IOException {
		abacPermit();

		Journalpost journalpost = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.U, JournalStatusCode.FS).build();
		joarkRepository.save(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		Long journalpostId = journalpost.getJournalpostId();
		FerdigstillJournalpostRequest request = FerdigstillJournalpostRequest.builder()
				.journalfEnhet("abc")
				.build();

		HttpEntity requestEntity = new HttpEntity("{ \"journalFEnhet\": \"9999\" }", createHeadersWithUserAndServiceUserToken());
		ResponseEntity<String> response =
				restTemplate.exchange(URL_FERDIGSTILLJOURNALPOST + journalpostId + FERDIGSTILL, HttpMethod.PATCH,
						requestEntity, String.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertThat(response.getBody(), containsString("JSON parse error: Unrecognized field \\\"journalFEnhet\\\" "));
	}

	@Test
	public void shouldFailIfRequestJournalpostIdIsInvalid() throws IOException {
		abacPermit();

		Journalpost journalpost = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.U, JournalStatusCode.FS).build();
		joarkRepository.save(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		String journalpostId = "abc";
		FerdigstillJournalpostRequest request = FerdigstillJournalpostRequest.builder()
				.journalfoerendeEnhet("9999")
				.build();

		HttpEntity requestEntity = new HttpEntity(request, createHeadersWithServiceUserToken());
		ResponseEntity<String> response =
				restTemplate.exchange(URL_FERDIGSTILLJOURNALPOST + journalpostId + FERDIGSTILL, HttpMethod.PATCH,
						requestEntity, String.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}

	@Test
	public void shouldFailIfMissingPaakrevdFelter() throws IOException {
		abacPermit();

		Journalpost journalpost = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.M).build();
		journalpost.setAvsenderMottaker(null);
		journalpost.setInnhold(null);
		joarkRepository.save(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		Long journalpostId = journalpost.getJournalpostId();
		FerdigstillJournalpostRequest request = FerdigstillJournalpostRequest.builder()
				.journalfoerendeEnhet("9999")
				.build();

		HttpEntity requestEntity = new HttpEntity(request, createHeadersWithServiceUserToken());
		ResponseEntity<RestConsumerExceptionResponse> response =
				restTemplate.exchange(URL_FERDIGSTILLJOURNALPOST + journalpostId + FERDIGSTILL, HttpMethod.PATCH,
						requestEntity, RestConsumerExceptionResponse.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertNotNull(response.getBody());
		assertTrue(response.getBody().getMessage().contains("Journalpost.avsendMottaker"));
		assertTrue(response.getBody().getMessage().contains("Journalpost.innhold"));
	}
}