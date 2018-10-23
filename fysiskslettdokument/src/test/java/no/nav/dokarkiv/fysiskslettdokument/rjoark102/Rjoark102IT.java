package no.nav.dokarkiv.fysiskslettdokument.rjoark102;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import no.nav.dokarkiv.fysiskslettdokument.AbstractFysiskSlettDokumentIT;
import no.nav.dokarkiv.fysiskslettdokument.util.TestUtils;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import javax.inject.Inject;
import java.util.List;

public class Rjoark102IT extends AbstractFysiskSlettDokumentIT {


	@Inject
	private JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;

	@Test
	public void shouldFysiskSlettAvKunEttVedlegg() {
		abacPermit();

		List<Journalpost> listeMedJournalposter = TestUtils.opprettHoveddokumentOgVedlegg(2);
		joarkRepository.saveAll(listeMedJournalposter);

		Journalpost journalpostSomSkalSlettes = listeMedJournalposter.get(1);
		DokumentInfo dokumentInfoSomSkalSlettes = journalpostDokumentInfoRelasjonRepository
				.findByJournalpostId(journalpostSomSkalSlettes.getJournalpostId()).getDokumentInfo();
		TestUtils.setLogiskSlettetByDokumentInfo(dokumentInfoSomSkalSlettes);

		TestTransaction.flagForCommit();
		TestTransaction.end();

//		TODO: Erstatt med HjemmelCode
		String hjemmel = "slettKunEttVedleggFraForsendeleKnyttetJP";

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKSLETTDOKUMENT + journalpostSomSkalSlettes.getJournalpostId() + "/" + dokumentInfoSomSkalSlettes.getDokumentInfoId() + "/" + hjemmel,
				HttpMethod.DELETE,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		assertThat(journalpostDokumentInfoRelasjonRepository.count(), is(2L));
		assertFalse(journalpostDokumentInfoRelasjonRepository.findByDokumentInfoId(dokumentInfoSomSkalSlettes.getDokumentInfoId())
				.isPresent());
		assertThat(dokumentinfoRepository.count(), is(2L));
		assertFalse(dokumentinfoRepository.findById(dokumentInfoSomSkalSlettes.getDokumentInfoId()).isPresent());
		assertThat(joarkRepository.count(), is(3L));
		assertTrue(joarkRepository.findById(journalpostSomSkalSlettes.getJournalpostId()).isPresent());
	}

	@Test
	public void shouldFailToFysiskSlettAvKunEttVedleggBecasueDokumentErIkkeTilknyttetSomVedlegg() {
		abacPermit();

		List<Journalpost> listeMedJournalposter = TestUtils.opprettHoveddokumentOgVedlegg(2);
		joarkRepository.saveAll(listeMedJournalposter);

		Journalpost journalpostSomSkalSlettes = listeMedJournalposter.get(0);
		DokumentInfo dokumentInfoSomSkalSlettes = journalpostDokumentInfoRelasjonRepository
				.findByJournalpostId(journalpostSomSkalSlettes.getJournalpostId()).getDokumentInfo();
		TestUtils.setLogiskSlettetByDokumentInfo(dokumentInfoSomSkalSlettes);

		TestTransaction.flagForCommit();
		TestTransaction.end();

//		TODO: Erstatt med HjemmelCode
		String hjemmel = "slettKunEttVedleggFraForsendeleKnyttetJP";

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKSLETTDOKUMENT + journalpostSomSkalSlettes.getJournalpostId() + "/" + dokumentInfoSomSkalSlettes.getDokumentInfoId() + "/" + hjemmel,
				HttpMethod.DELETE,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));

		assertThat(journalpostDokumentInfoRelasjonRepository.count(), is(3L));
		assertTrue(journalpostDokumentInfoRelasjonRepository.findByDokumentInfoId(dokumentInfoSomSkalSlettes.getDokumentInfoId())
				.isPresent());
		assertThat(dokumentinfoRepository.count(), is(3L));
		assertTrue(dokumentinfoRepository.findById(dokumentInfoSomSkalSlettes.getDokumentInfoId()).isPresent());
		assertThat(joarkRepository.count(), is(3L));
		assertTrue(joarkRepository.findById(journalpostSomSkalSlettes.getJournalpostId()).isPresent());

	}
}
