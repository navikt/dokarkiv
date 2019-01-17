package no.nav.dokarkiv.logiskkassasjon.rjoark106;

import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static no.nav.dokarkiv.logiskkassasjon.util.TestUtils.knyttDokumentInfoSomVedleggTilJournalpostForIT;
import static no.nav.dokarkiv.logiskkassasjon.util.TestUtils.opprettHoveddokumentForIT;
import static no.nav.dokarkiv.logiskkassasjon.util.TestUtils.opprettHoveddokumentMedEtKnyttetVedleggForIT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;

import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.logiskkassasjon.AbstractLogiskKassasjonIT;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.util.Optional;

public class Rjoark106IT extends AbstractLogiskKassasjonIT {

	@Test
	public void skalIkkeAngreLogiskKasseringAvDokument_ettersomDokumentInfoIdIkkeFinnes() {
		abacPermit();

		Long dokumentInfoId = 13L;

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_ANGRE_LOGISKKASSASJON + dokumentInfoId,
				HttpMethod.POST,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getBody(), containsString("DokumentInfo ikke funnet. dokumentInfoId=" + dokumentInfoId));
	}

	@Test
	public void skalIkkeAngreLogiskKasseringAvDokument_ettersomDokumentIkkeErKassert() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_ANGRE_LOGISKKASSASJON + dokumentInfo.getDokumentInfoId(),
				HttpMethod.POST,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getBody(), containsString(
				String.format("Fant ikke forventet begrensning for dokument med dokumentInfoId=%s og begrensningsType=%s",
						dokumentInfo.getDokumentInfoId(),
						BegrensningTypeCode.POL)));
	}

	@Test
	public void skalAngreLogiskKasseringAvDokument_medVedleggKnyttetFlereJournalposter() {
		abacPermit();

		Journalpost journalpost1 = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());
		Journalpost journalpost2 = opprettHoveddokumentForIT();

		JournalpostDokumentInfoRelasjon rel= journalpost1.findDokumentInfoRelasjonByTilknyttetJournalpostSom(
				TilknyttetJournalpostSomCode.VEDLEGG).iterator().next();

		knyttDokumentInfoSomVedleggTilJournalpostForIT(rel.getDokumentInfo(), journalpost2);

		joarkRepository.save(journalpost2);

		begrensningService.setDokumentKassert(rel.getDokumentInfo(), BegrensningTypeCode.POL);
		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();

		Optional<JournalpostDokumentInfoRelasjon> relasjonRep = journalpostDokumentInfoRelasjonRepository.findById(rel.getId());

		assertThat(joarkRepository.count(), is(2L));
		assertThat(dokumentinfoRepository.count(), is(3L));
		assertTrue(rel.getDokumentInfo().isRelatedToMultipleJournalposts());
		assertTrue(relasjonRep.isPresent());
		assertTrue(begrensningService.isDokumentInfoKassert(relasjonRep.get().getDokumentInfo()));

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_ANGRE_LOGISKKASSASJON + rel.getDokumentInfo().getDokumentInfoId(),
				HttpMethod.POST,
				createHeaders(),
				String.class);
		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		relasjonRep = journalpostDokumentInfoRelasjonRepository.findById(relasjonRep.get().getId());
		assertTrue(relasjonRep.isPresent());
		assertFalse(begrensningService.isDokumentInfoKassert(relasjonRep.get().getDokumentInfo()));
	}

	@Test
	public void skalAngreLogiskKasseringAvDokument_medHoveddokumentKnyttetFlereJournalposter() {
		abacPermit();

		Journalpost journalpost1 = joarkRepository.save(opprettHoveddokumentForIT());
		Journalpost journalpost2 = opprettHoveddokumentForIT();

		DokumentInfo hoveddokument1 = journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		knyttDokumentInfoSomVedleggTilJournalpostForIT(hoveddokument1, journalpost2);

		joarkRepository.save(journalpost2);

		begrensningService.setDokumentKassert(hoveddokument1, BegrensningTypeCode.POL);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();
		Optional<JournalpostDokumentInfoRelasjon> relRep = journalpostDokumentInfoRelasjonRepository.findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(journalpost1.getJournalpostId(), hoveddokument1.getDokumentInfoId());
		assertThat(relRep.isPresent(), is(true));
		assertTrue(begrensningService.isDokumentInfoKassert(relRep.get().getDokumentInfo()));

		assertThat(joarkRepository.count(), is(2L));
		assertThat(dokumentinfoRepository.count(), is(2L));
		assertTrue(hoveddokument1.isRelatedToMultipleJournalposts());

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_ANGRE_LOGISKKASSASJON + hoveddokument1.getDokumentInfoId(),
				HttpMethod.POST,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		relRep = journalpostDokumentInfoRelasjonRepository.findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(journalpost1.getJournalpostId(), hoveddokument1.getDokumentInfoId());
		assertThat(relRep.isPresent(), is(true));
		assertTrue(begrensningService.isDokumentInfoKassert(relRep.get().getDokumentInfo()));
	}

	@Test
	public void noAccess() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		begrensningService.setDokumentKassert(dokumentInfo, BegrensningTypeCode.POL);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_ANGRE_LOGISKKASSASJON + dokumentInfo.getDokumentInfoId(),
				HttpMethod.POST,
				createNoAccessHeaders(),
				String.class);


		assertThat(responseEntity.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
	}
}
