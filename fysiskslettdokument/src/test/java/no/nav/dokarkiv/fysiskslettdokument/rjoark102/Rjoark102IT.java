package no.nav.dokarkiv.fysiskslettdokument.rjoark102;

import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.HJEMMEL_VEDLEGG;
import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.createRequest;
import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.knyttDokumentInfoSomVedleggTilJournalpostForIT;
import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.opprettHoveddokumentForIT;
import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.oppretteDokumentMedEtVedleggForIT;
import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.oppretteDokumentOgKnyttVedleggForIt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import no.nav.dokarkiv.fysiskslettdokument.AbstractFysiskSlettDokumentIT;
import org.junit.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Set;

public class Rjoark102IT extends AbstractFysiskSlettDokumentIT {

	@Inject
	private JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;


	@Test
	public void shouldFysiskSlettEtVedleggKnyttetEnJP() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(oppretteDokumentMedEtVedleggForIT(false, true));

		TestTransaction.flagForCommit();
		TestTransaction.end();

		DokumentInfo dokumentInfoHoveddokument = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		DokumentInfo dokInfoVedlegg = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(
				TilknyttetJournalpostSomCode.VEDLEGG).iterator().next().getDokumentInfo();

		FysiskSlettDokumentRequestTo requestTo = createRequest(journalpost.getJournalpostId(), dokInfoVedlegg.getDokumentInfoId(), HJEMMEL_VEDLEGG);

		//TODO: Valider med responseEntity etter kvitteringsmelding er avklart
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKSLETTDOKUMENT + journalpost.getJournalpostId() + "/"
						+ dokInfoVedlegg.getDokumentInfoId() + "/" + HJEMMEL_VEDLEGG,
				HttpMethod.DELETE,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		assertTrue(journalpostDokumentInfoRelasjonRepository.findByJournalpostId(journalpost.getJournalpostId()).isPresent());
		assertTrue(journalpostDokumentInfoRelasjonRepository.findByDokumentInfoId(dokumentInfoHoveddokument.getDokumentInfoId())
				.isPresent());
		assertFalse(journalpostDokumentInfoRelasjonRepository.findByDokumentInfoId(dokInfoVedlegg.getDokumentInfoId())
				.isPresent());

		assertTrue(dokumentinfoRepository.findDokumentInfoByJournalpostIdAndDokumentInfoId(journalpost.getJournalpostId(), dokumentInfoHoveddokument
				.getDokumentInfoId()).isPresent());
		assertFalse(dokumentinfoRepository.findDokumentInfoByJournalpostIdAndDokumentInfoId(journalpost.getJournalpostId(), dokInfoVedlegg
				.getDokumentInfoId()).isPresent());

		assertTrue(joarkRepository.findAllJournalpostIdsByDokumentInfoId(dokumentInfoHoveddokument.getDokumentInfoId())
				.size() > 0);
		assertFalse(joarkRepository.findAllJournalpostIdsByDokumentInfoId(dokInfoVedlegg.getDokumentInfoId()).size() > 0);

	}

	@Test
	public void shouldFailToFysiskSlettEtVedleggKnyttetEnJPBecauseVedleggKnyttetTilFlereJournalposter() {
		abacPermit();

		Journalpost journalpost1 = joarkRepository.save(oppretteDokumentMedEtVedleggForIT(false, true));
		Journalpost journalpost2 = joarkRepository.save(opprettHoveddokumentForIT(false));

		DokumentInfo hoveddokument1 = journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		DokumentInfo hoveddokument2 = journalpost2.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		DokumentInfo vedlegg = journalpost1.findDokumentInfoRelasjonByTilknyttetJournalpostSom(
				TilknyttetJournalpostSomCode.VEDLEGG).iterator().next().getDokumentInfo();

		knyttDokumentInfoSomVedleggTilJournalpostForIT(vedlegg, journalpost2);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		FysiskSlettDokumentRequestTo requestTo = createRequest(journalpost1.getJournalpostId(), vedlegg.getDokumentInfoId(), HJEMMEL_VEDLEGG);

		//TODO: Valider med responseEntity etter kvitteringsmelding er avklart
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKSLETTDOKUMENT + journalpost1.getJournalpostId() + "/"
						+ vedlegg.getDokumentInfoId() + "/" + HJEMMEL_VEDLEGG,
				HttpMethod.DELETE,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(
				String.format("%s kan ikke slette et dokument som er knyttet til flere journalposter. dokumentInfoId=%s har relasjoner med %s journalposter.",
						MDC.get(MDCConstants.MDC_REQUEST_ID),
						vedlegg.getDokumentInfoId(),
						2)));


//		assertTrue(journalpostDokumentInfoRelasjonRepository.findByJournalpostId(journalpost.getJournalpostId()).isPresent());
//		assertTrue(journalpostDokumentInfoRelasjonRepository.findByDokumentInfoId(hoveddokument1.getDokumentInfoId()).isPresent());
//		assertFalse(journalpostDokumentInfoRelasjonRepository.findByDokumentInfoId(vedlegg.getDokumentInfoId()).isPresent());
//
//		assertTrue(dokumentinfoRepository.findDokumentInfoByJournalpostIdAndDokumentInfoId(journalpost.getJournalpostId(), hoveddokument1.getDokumentInfoId()).isPresent());
//		assertFalse(dokumentinfoRepository.findDokumentInfoByJournalpostIdAndDokumentInfoId(journalpost.getJournalpostId(), vedlegg.getDokumentInfoId()).isPresent());
//
//		assertTrue(joarkRepository.findAllJournalpostIdsByDokumentInfoId(hoveddokument1.getDokumentInfoId()).size()>0);
//		assertFalse(joarkRepository.findAllJournalpostIdsByDokumentInfoId(vedlegg.getDokumentInfoId()).size()>0);
//
	}

	@Test
	public void shouldFysiskSlettEtAvMangeVedleggKnyttetEnJP() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(oppretteDokumentOgKnyttVedleggForIt(false, 3));

		DokumentInfo dokumentInfoHoveddokument = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		Set<JournalpostDokumentInfoRelasjon> setRelasjoner =
				journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG);

		ArrayList<JournalpostDokumentInfoRelasjon> jpDokInfoRelasjonList = new ArrayList<>(setRelasjoner);

		DokumentInfo vedlegg1 = jpDokInfoRelasjonList.get(0).getDokumentInfo();
		DokumentInfo vedlegg2 = jpDokInfoRelasjonList.get(1).getDokumentInfo();
		DokumentInfo vedlegg3 = jpDokInfoRelasjonList.get(2).getDokumentInfo();

		vedlegg2.setSlettet(true);

		FysiskSlettDokumentRequestTo requestTo = createRequest(journalpost.getJournalpostId(), vedlegg2.getDokumentInfoId(), HJEMMEL_VEDLEGG);

		TestTransaction.flagForCommit();
		TestTransaction.end();


		//TODO: Valider med responseEntity etter kvitteringsmelding er avklart
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKSLETTDOKUMENT + journalpost.getJournalpostId() + "/"
						+ vedlegg2.getDokumentInfoId() + "/" + HJEMMEL_VEDLEGG,
				HttpMethod.DELETE,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		assertTrue(journalpostDokumentInfoRelasjonRepository.findByJournalpostId(journalpost.getJournalpostId()).isPresent());
		assertTrue(journalpostDokumentInfoRelasjonRepository.findByDokumentInfoId(dokumentInfoHoveddokument.getDokumentInfoId())
				.isPresent());
		assertTrue(journalpostDokumentInfoRelasjonRepository.findByDokumentInfoId(vedlegg1.getDokumentInfoId()).isPresent());
		assertFalse(journalpostDokumentInfoRelasjonRepository.findByDokumentInfoId(vedlegg2.getDokumentInfoId()).isPresent());
		assertTrue(journalpostDokumentInfoRelasjonRepository.findByDokumentInfoId(vedlegg3.getDokumentInfoId()).isPresent());

		assertTrue(dokumentinfoRepository.findDokumentInfoByJournalpostIdAndDokumentInfoId(journalpost.getJournalpostId(), dokumentInfoHoveddokument
				.getDokumentInfoId()).isPresent());
		assertTrue(dokumentinfoRepository.findDokumentInfoByJournalpostIdAndDokumentInfoId(journalpost.getJournalpostId(), vedlegg1
				.getDokumentInfoId()).isPresent());
		assertFalse(dokumentinfoRepository.findDokumentInfoByJournalpostIdAndDokumentInfoId(journalpost.getJournalpostId(), vedlegg2
				.getDokumentInfoId()).isPresent());
		assertTrue(dokumentinfoRepository.findDokumentInfoByJournalpostIdAndDokumentInfoId(journalpost.getJournalpostId(), vedlegg3
				.getDokumentInfoId()).isPresent());

		assertTrue(joarkRepository.findAllJournalpostIdsByDokumentInfoId(dokumentInfoHoveddokument.getDokumentInfoId())
				.size() > 0);
		assertTrue(joarkRepository.findAllJournalpostIdsByDokumentInfoId(vedlegg1.getDokumentInfoId()).size() > 0);
		assertFalse(joarkRepository.findAllJournalpostIdsByDokumentInfoId(vedlegg2.getDokumentInfoId()).size() > 0);
		assertTrue(joarkRepository.findAllJournalpostIdsByDokumentInfoId(vedlegg3.getDokumentInfoId()).size() > 0);
	}


	// IT test av validerKunEnGyldigRelasjonFoundByDokumentId -- trur ikke denne trengs

	// Gyldig slett av hoveddokument
//	@Test
//	public void shouldFysiskSlettEtHoveddokumentKnyttetEnJP() {
//		abacPermit();
//
//		String svett = "svettigt verre";
//		Journalpost journalpost = joarkRepository.save(oppretteDokumentOgKnyttVedleggForIt(true, 0));
//
//		DokumentInfo hoveddokument = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
//
//		FysiskSlettDokumentRequestTo requestTo = createRequest(journalpost.getJournalpostId(), hoveddokument.getDokumentInfoId(), HJEMMEL_HOVEDDOKUMENT);
//
//		TestTransaction.flagForCommit();
//		TestTransaction.end();
//
//
//		//TODO: Valider med responseEntity etter kvitteringsmelding er avklart
//		ResponseEntity<String> responseEntity = restTemplate.exchange(
//				URL_FYSISKSLETTDOKUMENT + journalpost.getJournalpostId() + "/"
//						+ hoveddokument.getDokumentInfoId() + "/" + HJEMMEL_VEDLEGG,
//				HttpMethod.DELETE,
//				createHeaders(),
//				String.class);
//
//		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
//
//		assertTrue(journalpostDokumentInfoRelasjonRepository.findByJournalpostId(journalpost.getJournalpostId()).isPresent());
//		assertTrue(journalpostDokumentInfoRelasjonRepository.findByDokumentInfoId(dokumentInfoHoveddokument.getDokumentInfoId()).isPresent());
//		assertTrue(journalpostDokumentInfoRelasjonRepository.findByDokumentInfoId(vedlegg1.getDokumentInfoId()).isPresent());
//		assertFalse(journalpostDokumentInfoRelasjonRepository.findByDokumentInfoId(vedlegg2.getDokumentInfoId()).isPresent());
//		assertTrue(journalpostDokumentInfoRelasjonRepository.findByDokumentInfoId(vedlegg3.getDokumentInfoId()).isPresent());
//
//		assertTrue(dokumentinfoRepository.findDokumentInfoByJournalpostIdAndDokumentInfoId(journalpost.getJournalpostId(), dokumentInfoHoveddokument.getDokumentInfoId()).isPresent());
//		assertTrue(dokumentinfoRepository.findDokumentInfoByJournalpostIdAndDokumentInfoId(journalpost.getJournalpostId(), vedlegg1.getDokumentInfoId()).isPresent());
//		assertFalse(dokumentinfoRepository.findDokumentInfoByJournalpostIdAndDokumentInfoId(journalpost.getJournalpostId(), vedlegg2.getDokumentInfoId()).isPresent());
//		assertTrue(dokumentinfoRepository.findDokumentInfoByJournalpostIdAndDokumentInfoId(journalpost.getJournalpostId(), vedlegg3.getDokumentInfoId()).isPresent());
//
//		assertTrue(joarkRepository.findAllJournalpostIdsByDokumentInfoId(dokumentInfoHoveddokument.getDokumentInfoId()).size() > 0);
//		assertTrue(joarkRepository.findAllJournalpostIdsByDokumentInfoId(vedlegg1.getDokumentInfoId()).size() > 0);
//		assertFalse(joarkRepository.findAllJournalpostIdsByDokumentInfoId(vedlegg2.getDokumentInfoId()).size() > 0);
//		assertTrue( joarkRepository.findAllJournalpostIdsByDokumentInfoId(vedlegg3.getDokumentInfoId()).size() > 0);
//	}

//	 Gyldig slett av hoveddokument og alle vedlegg

//	 Ikke gyldig slett av hoveddokument
	// Hoveddokument er vedlegg i annen forsendelse

	// Ikke gyldig slett av hoveddokument
	// Vedlegg er vedelgg i en annen forsendelse
	// Vedlegg er ikke satt til slett


	/**
	@Test public void shouldFysiskSlettAvKunEttVedleggKnyttetJP() {
		abacPermit();

	Journalpost jpHoveddokument = joarkRepository.save(TestUtils.opprettDokumentForIT(false));
	Journalpost jpVedlegg1 = joarkRepository.save(TestUtils.opprettDokumentForIT(true));

	TestUtils.knyttJournalpostSomVedleggTilJournalpostForIT(jpVedlegg1, jpHoveddokument);

	journalpostDokumentInfoRelasjonRepository.saveAll(jpHoveddokument.getJournalpostDokumentInfoRelasjoner());




		ResponseEntity<String> responseEntity = restTemplate.exchange(
	URL_FYSISKSLETTDOKUMENT + jpVedlegg1.getJournalpostId() + "/"
	+ jpVedlegg1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId()
	+ "/" + HJEMMEL_VEDLEGG,
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
	//						+ dokumentInfoSomSkalSlettes.getDokumentInfoId() + "/" + HJEMMEL_VEDLEGG,
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
	dokumentInfoSomSkalSlettes.getDokumentInfoId() + "/" + HJEMMEL_VEDLEGG,
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
	dokumentInfoId + "/" + HJEMMEL_HOVEDDOKUMENT,
				HttpMethod.DELETE,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		assertThat(journalpostDokumentInfoRelasjonRepository.count(), is(0L));
		assertThat(dokumentinfoRepository.count(), is(0L));
		assertThat(joarkRepository.count(), is(0L));
	}




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
	 dokumentInfoHoveddokument.getDokumentInfoId() + "/" + HJEMMEL_HOVEDDOKUMENT,
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
