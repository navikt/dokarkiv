package no.nav.dokarkiv.fysiskslettdokument.rjoark102;

import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.BEGRENSNINGTYPE_UTILGJENGELIGGJORT;
import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.opprettDuplikatRelasjon;
import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.opprettHoveddokumentForIT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;

import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import no.nav.dokarkiv.fysiskslettdokument.AbstractFysiskSlettDokumentIT;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import javax.inject.Inject;

public class Rjoark102IT extends AbstractFysiskSlettDokumentIT {

	@Inject
	private JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;

	@Test
	public void skalIkkeSletteDokumentFysisk_ettersomJournalpostDokumentInfoRelasjonMangler() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());
		Long feilDokumentInfoId = 13L;

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKSLETTDOKUMENT + journalpost.getJournalpostId() + "/"
						+ feilDokumentInfoId + "/" + BEGRENSNINGTYPE_UTILGJENGELIGGJORT,
				HttpMethod.DELETE,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getBody(), containsString(
				String.format("Kan ikke finne journalpostDokumentInfoRelasjon med journalpostId=%s og dokumentInfoId=%s",
						journalpost.getJournalpostId(),
						feilDokumentInfoId)));
	}

	@Test
	public void skalIkkeSletteDokumentFysisk_ettersomJournalpostDokumentInfoRelasjonIkkeErUnik() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());
		opprettDuplikatRelasjon(journalpost.findHoveddokumentDokumentInfoRelasjon());

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKSLETTDOKUMENT + journalpost.getJournalpostId() + "/"
						+ journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId() +
						"/" + BEGRENSNINGTYPE_UTILGJENGELIGGJORT,
				HttpMethod.DELETE,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getBody(), containsString(
				String.format("JournalpostDokumentInfoRelasjon med journalpostId=%s og dokumentInfoId=%s er ikke unikt",
						journalpost.getJournalpostId(),
						journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId())));
	}

	@Test
	public void skalIkkeSletteDokumentFysisk_medHoveddokument_ettersomHoveddokumentIkkeErUtilgjengeliggjort() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKSLETTDOKUMENT + journalpost.getJournalpostId() + "/"
						+ journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId() +
						"/" + BEGRENSNINGTYPE_UTILGJENGELIGGJORT,
				HttpMethod.DELETE,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(
				String.format("Fant ikke forventet begrensning for journalpost med journalpostId=%s og begrensningsType=%s.",
						journalpost.getJournalpostId(),
						BegrensningTypeCode.UTILGJENGELIGGJORT)));
	}


	// SLETTELINJE -----------------------------------------------------------------------





	// fysiskSlettEtVedleggKnyttetEnJP ---------------------------------------------

//	@Test
//	public void shouldFysiskSlettEtVedleggKnyttetEnJP() {
//		abacPermit();
//
//		Journalpost journalpost = joarkRepository.save(oppretteDokumentMedEtVedleggForIT(false, true));
//
//		TestTransaction.flagForCommit();
//		TestTransaction.end();
//
//		DokumentInfo dokumentInfoHoveddokument = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
//		DokumentInfo dokInfoVedlegg = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(
//				TilknyttetJournalpostSomCode.VEDLEGG).iterator().next().getDokumentInfo();
//
//		FysiskSlettDokumentRequestTo requestTo =
//				createRequest(journalpost.getJournalpostId(), dokInfoVedlegg.getDokumentInfoId(), BEGRENSNINGTYPE_UTILGJENGELIGGJORT);
//
//		//TODO: Valider med responseEntity etter kvitteringsmelding er avklart
//		ResponseEntity<String> responseEntity = restTemplate.exchange(
//				URL_FYSISKSLETTDOKUMENT + journalpost.getJournalpostId() + "/"
//						+ dokInfoVedlegg.getDokumentInfoId() + "/" + BEGRENSNINGTYPE_UTILGJENGELIGGJORT,
//				HttpMethod.DELETE,
//				createHeaders(),
//				String.class);
//
//		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
//
//		assertTrue(journalpostDokumentInfoRelasjonRepository
//				.findAllByJournalpostJournalpostId(journalpost.getJournalpostId()).isPresent());
//		assertTrue(journalpostDokumentInfoRelasjonRepository
//				.findAllByDokumentInfoDokumentInfoId(dokumentInfoHoveddokument.getDokumentInfoId()).isPresent());
//		assertFalse(journalpostDokumentInfoRelasjonRepository
//				.findAllByDokumentInfoDokumentInfoId(dokInfoVedlegg.getDokumentInfoId()).isPresent());
//
//		assertTrue(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
//				journalpost.getJournalpostId(), dokumentInfoHoveddokument.getDokumentInfoId()).isPresent());
//		assertFalse(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
//				journalpost.getJournalpostId(), dokInfoVedlegg.getDokumentInfoId()).isPresent());
//
//		assertTrue(joarkRepository.findAllJournalpostIdsByDokumentInfoId(
//				dokumentInfoHoveddokument.getDokumentInfoId()).size() > 0);
//		assertFalse(joarkRepository.findAllJournalpostIdsByDokumentInfoId(
//				dokInfoVedlegg.getDokumentInfoId()).size() > 0);
//	}
//
//	@Test
//	public void shouldFailToFysiskSlettEtVedleggKnyttetEnJPBecauseVedleggKnyttetTilFlereJournalposter() {
//		abacPermit();
//
//		Journalpost journalpost1 = joarkRepository.save(oppretteDokumentMedEtVedleggForIT(false, true));
//		Journalpost journalpost2 = joarkRepository.save(opprettHoveddokumentForIT(false));
//
//		DokumentInfo vedlegg = journalpost1.findDokumentInfoRelasjonByTilknyttetJournalpostSom(
//				TilknyttetJournalpostSomCode.VEDLEGG).iterator().next().getDokumentInfo();
//
//		knyttDokumentInfoSomVedleggTilJournalpostForIT(vedlegg, journalpost2);
//
//		TestTransaction.flagForCommit();
//		TestTransaction.end();
//
//		FysiskSlettDokumentRequestTo requestTo =
//				createRequest(journalpost1.getJournalpostId(), vedlegg.getDokumentInfoId(), BEGRENSNINGTYPE_UTILGJENGELIGGJORT);
//
//		//TODO: Valider med responseEntity etter kvitteringsmelding er avklart
//		ResponseEntity<String> responseEntity = restTemplate.exchange(
//				URL_FYSISKSLETTDOKUMENT + journalpost1.getJournalpostId() + "/"
//						+ vedlegg.getDokumentInfoId() + "/" + BEGRENSNINGTYPE_UTILGJENGELIGGJORT,
//				HttpMethod.DELETE,
//				createHeaders(),
//				String.class);
//
//		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
//		assertThat(responseEntity.getBody(), containsString(String.format(
//				"Kan ikke slette dokument med dokumentInfoId=%s fordi dokumentet er knyttet til flere journalposter.",
//				vedlegg.getDokumentInfoId())));
//	}
//
//	@Test
//	public void shouldFysiskSlettEtAvMangeVedleggKnyttetEnJP() {
//		abacPermit();
//
//		Journalpost journalpost = joarkRepository.save(oppretteDokumentOgKnyttVedleggForIt(false, 3));
//
//		DokumentInfo dokumentInfoHoveddokument = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
//
//		Set<JournalpostDokumentInfoRelasjon> setRelasjoner =
//				journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG);
//
//		ArrayList<JournalpostDokumentInfoRelasjon> jpDokInfoRelasjonList = new ArrayList<>(setRelasjoner);
//
//		DokumentInfo vedlegg1 = jpDokInfoRelasjonList.get(0).getDokumentInfo();
//		DokumentInfo vedlegg2 = jpDokInfoRelasjonList.get(1).getDokumentInfo();
//		DokumentInfo vedlegg3 = jpDokInfoRelasjonList.get(2).getDokumentInfo();
//
//		vedlegg2.setSlettet(true);
//
//		FysiskSlettDokumentRequestTo requestTo =
//				createRequest(journalpost.getJournalpostId(), vedlegg2.getDokumentInfoId(), BEGRENSNINGTYPE_UTILGJENGELIGGJORT);
//
//		TestTransaction.flagForCommit();
//		TestTransaction.end();
//
//
//		//TODO: Valider med responseEntity etter kvitteringsmelding er avklart
//		ResponseEntity<String> responseEntity = restTemplate.exchange(
//				URL_FYSISKSLETTDOKUMENT + journalpost.getJournalpostId() + "/"
//						+ vedlegg2.getDokumentInfoId() + "/" + BEGRENSNINGTYPE_UTILGJENGELIGGJORT,
//				HttpMethod.DELETE,
//				createHeaders(),
//				String.class);
//
//		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
//
//		assertTrue(journalpostDokumentInfoRelasjonRepository
//				.findAllByJournalpostJournalpostId(journalpost.getJournalpostId()).isPresent());
//		assertTrue(journalpostDokumentInfoRelasjonRepository
//				.findAllByDokumentInfoDokumentInfoId(dokumentInfoHoveddokument.getDokumentInfoId()).isPresent());
//		assertTrue(journalpostDokumentInfoRelasjonRepository
//				.findAllByDokumentInfoDokumentInfoId(vedlegg1.getDokumentInfoId()).isPresent());
//		assertFalse(journalpostDokumentInfoRelasjonRepository
//				.findAllByDokumentInfoDokumentInfoId(vedlegg2.getDokumentInfoId()).isPresent());
//		assertTrue(journalpostDokumentInfoRelasjonRepository
//				.findAllByDokumentInfoDokumentInfoId(vedlegg3.getDokumentInfoId()).isPresent());
//
//		assertTrue(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
//				journalpost.getJournalpostId(), dokumentInfoHoveddokument.getDokumentInfoId()).isPresent());
//		assertTrue(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
//				journalpost.getJournalpostId(), vedlegg1.getDokumentInfoId()).isPresent());
//		assertFalse(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
//				journalpost.getJournalpostId(), vedlegg2.getDokumentInfoId()).isPresent());
//		assertTrue(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
//				journalpost.getJournalpostId(), vedlegg3.getDokumentInfoId()).isPresent());
//
//		assertTrue(joarkRepository.findAllJournalpostIdsByDokumentInfoId(
//				dokumentInfoHoveddokument.getDokumentInfoId()).size() > 0);
//		assertTrue(joarkRepository.findAllJournalpostIdsByDokumentInfoId(vedlegg1.getDokumentInfoId()).size() > 0);
//		assertFalse(joarkRepository.findAllJournalpostIdsByDokumentInfoId(vedlegg2.getDokumentInfoId()).size() > 0);
//		assertTrue(joarkRepository.findAllJournalpostIdsByDokumentInfoId(vedlegg3.getDokumentInfoId()).size() > 0);
//	}
}
