package no.nav.dokarkiv.logiskslettdokument.rjoark101;

import static no.nav.dokarkiv.logiskslettdokument.LogiskSlettDokumentRestController.REQUEST_ID;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.createJournalpostBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import no.nav.dokarkiv.logiskslettdokument.AbstractSlettDokumentIT;
import no.nav.dokarkiv.logiskslettdokument.rjoark100.LogiskSlettDokumentResponse;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import javax.inject.Inject;

public class Rjoark101IT extends AbstractSlettDokumentIT {

	@Inject
	private JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;

	@Test
	public void shouldAngreLogiskSlettDokument() {
		abacPermit();

		Journalpost journalpost = createJournalpostBuilder().build();
		setJournalpostSlettet(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<LogiskSlettDokumentResponse> responseEntity = restTemplate.exchange(URL_SLETTDOKUMENT + journalpost.getJournalpostId()
						+ "/" + journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId() + "/angre",
				HttpMethod.PATCH, createHeaders(), LogiskSlettDokumentResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		assertEquals(journalpostDokumentInfoRelasjonRepository.findByDokumentInfoId(
				journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId())
				.get()
				.get(0)
				.getDokumentInfo()
				.getSlettet(), false);
	}

	@Test
	public void shouldFailToAngreLogiskSlettDokumentBecauseDocumentWasNotDeleted() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(createJournalpostBuilder().build());

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(URL_SLETTDOKUMENT + journalpost.getJournalpostId()
						+ "/" + journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId() + "/angre",
				HttpMethod.PATCH, createHeaders(), String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(REQUEST_ID + " prøver å angre logisk sletting av et dokument " +
				"som ikke er logisk slettet, dokumentInfoId=" + journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId()));
		assertEquals(journalpostDokumentInfoRelasjonRepository.findByDokumentInfoId(journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo().getDokumentInfoId()).get()
				.get(0)
				.getDokumentInfo()
				.getSlettet(), false);

	}

	private void setJournalpostSlettet(Journalpost journalpost) {
		journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().setSlettet(true);
		joarkRepository.save(journalpost);
	}


}
