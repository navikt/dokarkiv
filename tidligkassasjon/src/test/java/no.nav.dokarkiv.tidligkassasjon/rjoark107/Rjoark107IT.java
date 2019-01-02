package no.nav.dokarkiv.tidligkassasjon.rjoark107;

import static no.nav.dokarkiv.tidligkassasjon.util.TestUtil.kassereDokumentLogisk;
import static no.nav.dokarkiv.tidligkassasjon.util.TestUtil.knyttDokumentInfoSomVedleggTilJournalpostForIT;
import static no.nav.dokarkiv.tidligkassasjon.util.TestUtil.opprettHoveddokumentForIT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.tidligkassasjon.AbstractTidligKassasjonIT;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

public class Rjoark107IT extends AbstractTidligKassasjonIT {

	@Test
	public void skallIkkeTidligtKassereDokument_ettersomDokmentInfoIdIkkeFinnes() {
		abacPermit();

		Long dokumentInfoId = 13L;

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_TIDLIGKASSASJON + dokumentInfoId,
				HttpMethod.POST,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getBody(), containsString("Kan ikke finne dokument med dokumentInfoId=" + dokumentInfoId));
	}

	@Test
	public void skallIkkeTidligtKassereDokument_ettersomDokmentInfoIkkeErLogiskKassert() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_TIDLIGKASSASJON + dokumentInfo.getDokumentInfoId(),
				HttpMethod.POST,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getBody(), containsString(String.format(
				"Fant ikke forventet begrensning for dokument med dokumentInfoId=%s og begrensningsType=%s",
				dokumentInfo.getDokumentInfoId(),
				BegrensningTypeCode.KASSERT)));
	}

	@Test
	public void skallTidligtKassereDokument_medDokmentKnyttetFlereJournalposter() {
		abacPermit();

		Journalpost journalpost1 = joarkRepository.save(opprettHoveddokumentForIT());
		Journalpost journalpost2 = opprettHoveddokumentForIT();
		DokumentInfo dokumentInfo1 = journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		knyttDokumentInfoSomVedleggTilJournalpostForIT(dokumentInfo1, journalpost2);

		begrensningRepository.save(kassereDokumentLogisk(dokumentInfo1));

		joarkRepository.save(journalpost2);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		assertThat("Feil antall begrensninger", begrensningRepository.count(), is(1L));
		assertThat("Feil antall journalposter", joarkRepository.count(), is(2L));
		assertThat("Feil antall dokumenter", dokumentinfoRepository.count(), is(2L));
		assertTrue(dokumentInfo1.isRelatedToMultipleJournalposts());

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_TIDLIGKASSASJON + dokumentInfo1.getDokumentInfoId(),
				HttpMethod.POST,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		assertThat("Feil antall begrensninger etter kall", begrensningRepository.count(), is(0L));
		assertThat("Feil antall journalposter etter kall", joarkRepository.count(), is(2L));
		assertThat("Feil antall dokumenter etter kall", dokumentinfoRepository.count(), is(2L));
	}

	@Test
	public void skallTidligtKassereDokument_medDokumentKnyttetEnJournalpost() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		begrensningRepository.save(kassereDokumentLogisk(dokumentInfo));

		TestTransaction.flagForCommit();
		TestTransaction.end();

		assertThat("Feil antall begrensninger", begrensningRepository.count(), is(1L));
		assertThat("Feil antall journalposter", joarkRepository.count(), is(1L));
		assertThat("Feil antall dokumenter", dokumentinfoRepository.count(), is(1L));
		assertFalse(dokumentInfo.isRelatedToMultipleJournalposts());
		assertFalse(dokumentInfo.getFildetaljerListe().isEmpty());

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_TIDLIGKASSASJON + dokumentInfo.getDokumentInfoId(),
				HttpMethod.POST,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		assertThat("Feil antall begrensninger etter kall", begrensningRepository.count(), is(0L));
		assertThat("Feil antall journalposter etter kall", joarkRepository.count(), is(1L));
		assertThat("Feil antall dokumenter etter kall", dokumentinfoRepository.count(), is(1L));
	}

	@Test
	public void noAccess() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		begrensningRepository.save(kassereDokumentLogisk(dokumentInfo));

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_TIDLIGKASSASJON + dokumentInfo.getDokumentInfoId(),
				HttpMethod.POST,
				createNoAccessHeaders(),
				String.class);


		assertThat(responseEntity.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
	}


}
