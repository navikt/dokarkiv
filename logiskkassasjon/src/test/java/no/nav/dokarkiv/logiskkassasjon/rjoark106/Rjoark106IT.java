package no.nav.dokarkiv.logiskkassasjon.rjoark106;

import static junit.framework.TestCase.assertTrue;
import static no.nav.dokarkiv.logiskkassasjon.util.TestUtils.kasserDokumentLogisk;
import static no.nav.dokarkiv.logiskkassasjon.util.TestUtils.knyttDokumentInfoSomVedleggTilJournalpostForIT;
import static no.nav.dokarkiv.logiskkassasjon.util.TestUtils.opprettHoveddokumentForIT;
import static no.nav.dokarkiv.logiskkassasjon.util.TestUtils.opprettHoveddokumentMedEtKnyttetVedleggForIT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;

import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.logiskkassasjon.AbstractLogiskKassasjonIT;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

public class Rjoark106IT extends AbstractLogiskKassasjonIT {

	@Test
	public void skalIkkeAngreLogiskKasseringAvDokument_ettersomDokumentInfoIdIkkeFinnes() {
		abacPermit();

		Long dokumentInfoId = 13L;

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_ANGRE_LOGISKKASSASJON + dokumentInfoId,
				HttpMethod.PATCH,
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
				HttpMethod.PATCH,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(
				String.format("Fant ikke forventet begrensning for dokument med dokumentInfoId=%s og begrensningsType=%s",
						dokumentInfo.getDokumentInfoId(),
						//TODO: Endre til begrensningsType for kassasjon
						BegrensningTypeCode.UTILGJENGELIGGJORT)));
	}

	@Test
	public void skalAngreLogiskKasseringAvDokument_medVedleggKnyttetFlereJournalposter() {
		abacPermit();

		Journalpost journalpost1 = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());
		Journalpost journalpost2 = opprettHoveddokumentForIT();

		DokumentInfo vedlegg = journalpost1.findDokumentInfoRelasjonByTilknyttetJournalpostSom(
				TilknyttetJournalpostSomCode.VEDLEGG).iterator().next().getDokumentInfo();

		knyttDokumentInfoSomVedleggTilJournalpostForIT(vedlegg, journalpost2);

		joarkRepository.save(journalpost2);

		begrensningRepository.save(kasserDokumentLogisk(vedlegg));

		TestTransaction.flagForCommit();
		TestTransaction.end();

		assertThat(begrensningRepository.count(), is(1L));
		assertThat(joarkRepository.count(), is(2L));
		assertThat(dokumentinfoRepository.count(), is(3L));

		assertTrue(vedlegg.isRelatedToMultipleJournalposts());

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_ANGRE_LOGISKKASSASJON + vedlegg.getDokumentInfoId(),
				HttpMethod.PATCH,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		assertThat(begrensningRepository.count(), is(0L));
	}
}
