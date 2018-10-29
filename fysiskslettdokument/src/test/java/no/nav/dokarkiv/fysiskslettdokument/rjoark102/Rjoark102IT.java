package no.nav.dokarkiv.fysiskslettdokument.rjoark102;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

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

public class Rjoark102IT extends AbstractFysiskSlettDokumentIT {

	//TODO: Erstatt med HjemmelCode
	private static final String FYSISKSLETT_KUN_ETT_VEDLEGG = "fysiskSlettAvKunEttVedleggKnyttetJP";
	private static final String FYSISKSLETT_KUN_ETT_HOVEDDOKUMENT = "fysiskSlettAvKunEttHoveddokumentKnyttetJP";

	@Inject
	private JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;

	/**
	@Test public void shouldFysiskSlettAvKunEttVedleggKnyttetJP() {
		abacPermit();

	Journalpost jpHoveddokument = joarkRepository.save(TestUtils.opprettDokumentForIT(false));
	Journalpost jpVedlegg1 = joarkRepository.save(TestUtils.opprettDokumentForIT(true));

	TestUtils.knyttJournalpostSomVedleggTilJournalpostForIT(jpVedlegg1, jpHoveddokument);

	journalpostDokumentInfoRelasjonRepository.saveAll(jpHoveddokument.getJournalpostDokumentInfoRelasjoner());

		TestTransaction.flagForCommit();
		TestTransaction.end();


		ResponseEntity<String> responseEntity = restTemplate.exchange(
	URL_FYSISKSLETTDOKUMENT + jpVedlegg1.getJournalpostId() + "/"
	+ jpVedlegg1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId()
	+ "/" + FYSISKSLETT_KUN_ETT_VEDLEGG,
				HttpMethod.DELETE,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
	//
	//		assertThat(journalpostDokumentInfoRelasjonRepository.count(), is(2L));
	//		assertFalse(journalpostDokumentInfoRelasjonRepository.findByDokumentInfoId(dokumentInfoSomSkalSlettes.getDokumentInfoId())
	//				.isPresent());
	//		assertThat(dokumentinfoRepository.count(), is(2L));
	//		assertFalse(dokumentinfoRepository.findById(dokumentInfoSomSkalSlettes.getDokumentInfoId()).isPresent());
	//		assertThat(joarkRepository.count(), is(3L));
	//		assertTrue(joarkRepository.findById(journalpostSomSkalSlettes.getJournalpostId()).isPresent());



	//		Journalpost vedlegg1 = TestUtils.opprettDokumentForIT(true);
	//		Journalpost vedlegg2 = TestUtils.opprettDokumentForIT(false);



	//		List<Journalpost> listeMedJournalposter = TestUtils.opprettHoveddokumentOgKnyttVedleggForEnhetstest(2);
	//		listeMedJournalposter.forEach(journalpost -> joarkRepository.save(journalpost));

	//		joarkRepository.saveAll(listeMedJournalposter);

	//		Journalpost journalpostSomSkalSlettes = listeMedJournalposter.get(1);
	//		DokumentInfo dokumentInfoSomSkalSlettes = journalpostDokumentInfoRelasjonRepository
	//				.findOneByJournalpostId(journalpostSomSkalSlettes.getJournalpostId()).getDokumentInfo();
	//		TestUtils.setLogiskSlettetByDokumentInfo(dokumentInfoSomSkalSlettes);

	//		TestTransaction.flagForCommit();
	//		TestTransaction.end();

	//		ResponseEntity<String> responseEntity = restTemplate.exchange(
	//				URL_FYSISKSLETTDOKUMENT + journalpostSomSkalSlettes.getJournalpostId() + "/"
	//						+ dokumentInfoSomSkalSlettes.getDokumentInfoId() + "/" + FYSISKSLETT_KUN_ETT_VEDLEGG,
	//				HttpMethod.DELETE,
	//				createHeaders(),
	//				String.class);

	//		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
	//
	//		assertThat(journalpostDokumentInfoRelasjonRepository.count(), is(2L));
	//		assertFalse(journalpostDokumentInfoRelasjonRepository.findByDokumentInfoId(dokumentInfoSomSkalSlettes.getDokumentInfoId())
	//				.isPresent());
	//		assertThat(dokumentinfoRepository.count(), is(2L));
	//		assertFalse(dokumentinfoRepository.findById(dokumentInfoSomSkalSlettes.getDokumentInfoId()).isPresent());
	//		assertThat(joarkRepository.count(), is(3L));
	//		assertTrue(joarkRepository.findById(journalpostSomSkalSlettes.getJournalpostId()).isPresent());
	}

	//Testes i FysiskSlettDokumentValidatorTest - Slett?
	@Test public void shouldFailToFysiskSlettAvKunEttVedleggKnyttetJPBecasueDokumentErIkkeTilknyttetSomVedlegg() {
		abacPermit();

	List<Journalpost> listeMedJournalposter = TestUtils.opprettHoveddokumentOgKnyttVedleggForEnhetstest(2);
		joarkRepository.saveAll(listeMedJournalposter);

		Journalpost journalpostSomSkalSlettes = listeMedJournalposter.get(0);
		DokumentInfo dokumentInfoSomSkalSlettes = journalpostDokumentInfoRelasjonRepository
	.findOneByJournalpostId(journalpostSomSkalSlettes.getJournalpostId()).getDokumentInfo();
		TestUtils.setLogiskSlettetByDokumentInfo(dokumentInfoSomSkalSlettes);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
	URL_FYSISKSLETTDOKUMENT + journalpostSomSkalSlettes.getJournalpostId() + "/" +
	dokumentInfoSomSkalSlettes.getDokumentInfoId() + "/" + FYSISKSLETT_KUN_ETT_VEDLEGG,
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
	 */

	@Test
	public void shouldFysiskSlettAvKunEttHoveddokumentKnyttetJP() {
		abacPermit();

		Journalpost jpHoveddokument = joarkRepository.save(TestUtils.opprettDokumentForIT(true));

		TestTransaction.flagForCommit();
		TestTransaction.end();

		Long dokumentInfoId = jpHoveddokument.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

		assertThat(journalpostDokumentInfoRelasjonRepository.count(), is(1L));
		assertThat(dokumentinfoRepository.count(), is(1L));
		assertThat(joarkRepository.count(), is(1L));

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKSLETTDOKUMENT + jpHoveddokument.getJournalpostId() + "/" +
						dokumentInfoId + "/" + FYSISKSLETT_KUN_ETT_HOVEDDOKUMENT,
				HttpMethod.DELETE,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		assertThat(journalpostDokumentInfoRelasjonRepository.count(), is(0L));
		assertThat(dokumentinfoRepository.count(), is(0L));
		assertThat(joarkRepository.count(), is(0L));
	}


/**

 @Test public void shouldFailToFysiskSlettAvKunEttHoveddokumentKnyttetJPBecasueHoveddokumentHarVedlegg() {
 abacPermit();

 List<Journalpost> listeMedJournalposter = TestUtils.opprettHoveddokumentOgKnyttVedleggForEnhetstest(1);
 joarkRepository.saveAll(listeMedJournalposter);

 Journalpost jpHoveddokument = listeMedJournalposter.get(0);
 assertTrue(jpHoveddokument.getThisJournalpostDokumentInfoRelasjon().isHoveddokument());

 DokumentInfo dokumentInfoHoveddokument = journalpostDokumentInfoRelasjonRepository
 .findOneByJournalpostId(jpHoveddokument.getJournalpostId()).getDokumentInfo();
 TestUtils.setLogiskSlettetByDokumentInfo(dokumentInfoHoveddokument);

 TestTransaction.flagForCommit();
 TestTransaction.end();

 ResponseEntity<String> responseEntity = restTemplate.exchange(
 URL_FYSISKSLETTDOKUMENT + jpHoveddokument.getJournalpostId() + "/" +
 dokumentInfoHoveddokument.getDokumentInfoId() + "/" + FYSISKSLETT_KUN_ETT_HOVEDDOKUMENT,
 HttpMethod.DELETE,
 createHeaders(),
 String.class);

 assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));

 //		assertThat(journalpostDokumentInfoRelasjonRepository.count(), is(2L));
 //		assertFalse(journalpostDokumentInfoRelasjonRepository.findByDokumentInfoId(dokumentInfoHoveddokument.getDokumentInfoId())
 //				.isPresent());
 //		assertThat(dokumentinfoRepository.count(), is(2L));
 //		assertFalse(dokumentinfoRepository.findById(dokumentInfoHoveddokument.getDokumentInfoId()).isPresent());
 //		assertThat(joarkRepository.count(), is(3L));
 //		assertTrue(joarkRepository.findById(jpHoveddokument.getJournalpostId()).isPresent());

 }

 */
}
