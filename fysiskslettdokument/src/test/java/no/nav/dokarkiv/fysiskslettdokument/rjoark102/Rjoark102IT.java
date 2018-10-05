package no.nav.dokarkiv.fysiskslettdokument.rjoark102;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import no.nav.dokarkiv.fysiskslettdokument.AbstractFysiskSlettDokumentIT;
import no.nav.dokarkiv.fysiskslettdokument.util.TestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import javax.inject.Inject;

public class Rjoark102IT extends AbstractFysiskSlettDokumentIT {

	@Inject
	private JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void shouldPhysicallyDeleteDocumentInJoark() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(TestUtils.createJournalpost(true));


		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<FysiskSlettDokumentResponse> responseEntity = restTemplate.exchange(URL_FYSISKSLETTDOKUMENT + journalpost
				.getJournalpostId() + "/"
				+ journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId() + "/" + "hjemmel", HttpMethod.DELETE, createHeaders(), FysiskSlettDokumentResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		assertEquals(journalpostDokumentInfoRelasjonRepository.findByDokumentInfoId(
				journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId()).get()
				.get(0)
				.getDokumentInfo().getSlettet(), true);
	}


}
