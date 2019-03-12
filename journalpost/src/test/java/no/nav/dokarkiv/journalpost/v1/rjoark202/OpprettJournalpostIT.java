package no.nav.dokarkiv.journalpost.v1.rjoark202;

import static no.nav.dokarkiv.journalpost.v1.api.JournalpostType.INNGAAENDE;
import static no.nav.dokarkiv.journalpost.v1.api.JournalpostType.NOTAT;
import static no.nav.dokarkiv.journalpost.v1.api.JournalpostType.UTGAAENDE;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createRequest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.journalpost.v1.api.JournalpostType;
import no.nav.dokarkiv.journalpost.v1.api.OpprettJournalpostRequest;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

public class OpprettJournalpostIT extends AbstractOpprettJournalpostIT {

	@Test
	public void happyPathInngaaende() throws IOException {
		abacPermit();

		OpprettJournalpostRequest request = createRequest(INNGAAENDE);

		HttpEntity requestEntity = new HttpEntity(request, createHeadersWithServiceUserToken());
		ResponseEntity<String> response = restTemplate.exchange(URL_OPPRETTJOURNALPOST, HttpMethod.POST, requestEntity, String.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());

		Journalpost journalpost = joarkRepository.findAll().iterator().next();
		assertNotNull(journalpost.getJournalpostId());
		assertEquals(JournalpostTypeCode.I, journalpost.getJournalposttype());
		assertEquals(JournalStatusCode.M, journalpost.getJournalstatus());
	}

	@Test
	public void happyPathUtgaaende() throws IOException {
		abacPermit();

		OpprettJournalpostRequest request = createRequest(UTGAAENDE);

		HttpEntity requestEntity = new HttpEntity(request, createHeadersWithServiceUserToken());
		ResponseEntity<String> response = restTemplate.exchange(URL_OPPRETTJOURNALPOST, HttpMethod.POST, requestEntity, String.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());

		Journalpost journalpost = joarkRepository.findAll().iterator().next();
		assertNotNull(journalpost.getJournalpostId());
		assertEquals(JournalpostTypeCode.U, journalpost.getJournalposttype());
		assertEquals(JournalStatusCode.D, journalpost.getJournalstatus());
	}


	@Test
	public void happyPathNotat() throws IOException {
		abacPermit();

		OpprettJournalpostRequest request = createRequest(NOTAT);

		HttpEntity requestEntity = new HttpEntity(request, createHeadersWithServiceUserToken());
		ResponseEntity<String> response = restTemplate.exchange(URL_OPPRETTJOURNALPOST, HttpMethod.POST, requestEntity, String.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());

		Journalpost journalpost = joarkRepository.findAll().iterator().next();
		assertNotNull(journalpost.getJournalpostId());
		assertEquals(JournalpostTypeCode.N, journalpost.getJournalposttype());
		assertEquals(JournalStatusCode.D, journalpost.getJournalstatus());
	}
}