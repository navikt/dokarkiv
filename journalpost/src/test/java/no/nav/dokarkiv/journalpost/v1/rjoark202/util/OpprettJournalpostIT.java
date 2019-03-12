package no.nav.dokarkiv.journalpost.v1.rjoark202.util;

import static org.junit.Assert.assertEquals;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.journalpost.v1.api.JournalpostType;
import no.nav.dokarkiv.journalpost.v1.api.OpprettJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.util.TestUtils;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.io.IOException;

public class OpprettJournalpostIT extends AbstractOpprettJournalpostIT {

	@Test
	public void happyPathInngaaende() throws IOException {
		abacPermit();

		OpprettJournalpostRequest request = TestUtils.createRequest(JournalpostType.INNGAAENDE);

		HttpEntity requestEntity = new HttpEntity(request, createHeadersWithServiceUserToken());
		ResponseEntity<String> response = restTemplate.exchange(URL_OPPRETTJOURNALPOST, HttpMethod.POST, requestEntity, String.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());

	}
}
