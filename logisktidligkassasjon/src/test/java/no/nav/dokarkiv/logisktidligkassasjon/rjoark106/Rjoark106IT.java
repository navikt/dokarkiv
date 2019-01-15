package no.nav.dokarkiv.logisktidligkassasjon.rjoark106;

import static junit.framework.TestCase.assertTrue;
import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_INFO_HEADER;
import static no.nav.dokarkiv.core.util.TestDataUtils.AKSJON_ANGRE_LOGISK_TIDLIG_KASSASJON;
import static no.nav.dokarkiv.logisktidligkassasjon.util.TestUtils.kassereDokumentLogisk;
import static no.nav.dokarkiv.logisktidligkassasjon.util.TestUtils.knyttDokumentInfoSomVedleggTilJournalpostForIT;
import static no.nav.dokarkiv.logisktidligkassasjon.util.TestUtils.opprettHoveddokumentForIT;
import static no.nav.dokarkiv.logisktidligkassasjon.util.TestUtils.opprettHoveddokumentMedEtKnyttetVedleggForIT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;

import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.logisktidligkassasjon.AbstractLogiskTidligKassasjonIT;
import org.apache.commons.collections15.IteratorUtils;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.io.IOException;
import java.util.List;

public class Rjoark106IT extends AbstractLogiskTidligKassasjonIT {

	@Test
	public void skalLagreAksjon() throws IOException {
		abacPermit();

		Journalpost journalpost1 = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());
		Journalpost journalpost2 = opprettHoveddokumentForIT();

		DokumentInfo vedlegg = journalpost1.findDokumentInfoRelasjonByTilknyttetJournalpostSom(
				TilknyttetJournalpostSomCode.VEDLEGG).iterator().next().getDokumentInfo();

		knyttDokumentInfoSomVedleggTilJournalpostForIT(vedlegg, journalpost2);

		joarkRepository.save(journalpost2);

		begrensningRepository.save(kassereDokumentLogisk(vedlegg));

		TestTransaction.flagForCommit();
		TestTransaction.end();

		assertThat(begrensningRepository.count(), is(1L));
		assertThat(joarkRepository.count(), is(2L));
		assertThat(dokumentinfoRepository.count(), is(3L));
		assertTrue(vedlegg.isRelatedToMultipleJournalposts());

		List<AksjonsLogg> aksjonsLoggListBefore = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggListBefore.size(), is(0));

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_ANGRE_LOGISKTIDLIGKASSASJON + vedlegg.getDokumentInfoId(),
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AKSJON_ANGRE_LOGISK_TIDLIG_KASSASJON)),
				String.class);
		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));
		assertThat(aksjonsLoggList.get(0).getAksjon(), is(AKSJON_ANGRE_LOGISK_TIDLIG_KASSASJON));
	}

	@Test
	public void skalFeileNårAksjonsLoggHeaderIkkeErSatt() throws IOException {
		abacPermit();

		Journalpost journalpost1 = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());
		Journalpost journalpost2 = opprettHoveddokumentForIT();

		DokumentInfo vedlegg = journalpost1.findDokumentInfoRelasjonByTilknyttetJournalpostSom(
				TilknyttetJournalpostSomCode.VEDLEGG).iterator().next().getDokumentInfo();

		knyttDokumentInfoSomVedleggTilJournalpostForIT(vedlegg, journalpost2);

		joarkRepository.save(journalpost2);
		begrensningRepository.save(kassereDokumentLogisk(vedlegg));

		TestTransaction.flagForCommit();
		TestTransaction.end();

		assertThat(begrensningRepository.count(), is(1L));
		assertThat(joarkRepository.count(), is(2L));
		assertThat(dokumentinfoRepository.count(), is(3L));
		assertTrue(vedlegg.isRelatedToMultipleJournalposts());

		List<AksjonsLogg> aksjonsLoggListBefore = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggListBefore.size(), is(0));

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_ANGRE_LOGISKTIDLIGKASSASJON + vedlegg.getDokumentInfoId(),
				HttpMethod.POST,
				createHeaders(),
				String.class);
		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(String.format("Missing request header '%s'", AKSJONS_INFO_HEADER)));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(0));
	}

	@Test
	public void skalIkkeLagreAksjonsLoggVedFeil() throws IOException {
		abacPermit();

		Journalpost journalpost1 = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());
		Journalpost journalpost2 = opprettHoveddokumentForIT();

		DokumentInfo vedlegg = journalpost1.findDokumentInfoRelasjonByTilknyttetJournalpostSom(
				TilknyttetJournalpostSomCode.VEDLEGG).iterator().next().getDokumentInfo();

		knyttDokumentInfoSomVedleggTilJournalpostForIT(vedlegg, journalpost2);

		joarkRepository.save(journalpost2);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		List<AksjonsLogg> aksjonsLoggListBefore = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggListBefore.size(), is(0));

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_ANGRE_LOGISKTIDLIGKASSASJON + vedlegg.getDokumentInfoId(),
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AKSJON_ANGRE_LOGISK_TIDLIG_KASSASJON)),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(0));
	}

	@Test
	public void skalIkkeAngreLogiskTidligKasseringAvDokument_ettersomDokumentInfoIdIkkeFinnes() throws IOException {
		abacPermit();

		Long dokumentInfoId = 13L;

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_ANGRE_LOGISKTIDLIGKASSASJON + dokumentInfoId,
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AKSJON_ANGRE_LOGISK_TIDLIG_KASSASJON)),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getBody(), containsString("DokumentInfo ikke funnet. dokumentInfoId=" + dokumentInfoId));
	}

	@Test
	public void skalIkkeAngreLogiskTidligKasseringAvDokument_ettersomDokumentIkkeErKassert() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_ANGRE_LOGISKTIDLIGKASSASJON + dokumentInfo.getDokumentInfoId(),
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AKSJON_ANGRE_LOGISK_TIDLIG_KASSASJON)),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getBody(), containsString(
				String.format("Fant ikke forventet begrensning for dokument med dokumentInfoId=%s og begrensningsType=%s",
						dokumentInfo.getDokumentInfoId(),
						BegrensningTypeCode.KASSERT)));
	}

	@Test
	public void skalAngreLogiskTidligKasseringAvDokument_medVedleggKnyttetFlereJournalposter() throws IOException {
		abacPermit();

		Journalpost journalpost1 = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());
		Journalpost journalpost2 = opprettHoveddokumentForIT();

		DokumentInfo vedlegg = journalpost1.findDokumentInfoRelasjonByTilknyttetJournalpostSom(
				TilknyttetJournalpostSomCode.VEDLEGG).iterator().next().getDokumentInfo();

		knyttDokumentInfoSomVedleggTilJournalpostForIT(vedlegg, journalpost2);

		joarkRepository.save(journalpost2);

		begrensningRepository.save(kassereDokumentLogisk(vedlegg));

		TestTransaction.flagForCommit();
		TestTransaction.end();

		assertThat(begrensningRepository.count(), is(1L));
		assertThat(joarkRepository.count(), is(2L));
		assertThat(dokumentinfoRepository.count(), is(3L));
		assertTrue(vedlegg.isRelatedToMultipleJournalposts());

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_ANGRE_LOGISKTIDLIGKASSASJON + vedlegg.getDokumentInfoId(),
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AKSJON_ANGRE_LOGISK_TIDLIG_KASSASJON)),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		assertThat(begrensningRepository.count(), is(0L));
	}

	@Test
	public void skalAngreLogiskTidligKasseringAvDokument_medHoveddokumentKnyttetFlereJournalposter() throws IOException {
		abacPermit();

		Journalpost journalpost1 = joarkRepository.save(opprettHoveddokumentForIT());
		Journalpost journalpost2 = opprettHoveddokumentForIT();

		DokumentInfo hoveddokument1 = journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		knyttDokumentInfoSomVedleggTilJournalpostForIT(hoveddokument1, journalpost2);

		joarkRepository.save(journalpost2);

		begrensningRepository.save(kassereDokumentLogisk(hoveddokument1));

		TestTransaction.flagForCommit();
		TestTransaction.end();

		assertThat(begrensningRepository.count(), is(1L));
		assertThat(joarkRepository.count(), is(2L));
		assertThat(dokumentinfoRepository.count(), is(2L));
		assertTrue(hoveddokument1.isRelatedToMultipleJournalposts());

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_ANGRE_LOGISKTIDLIGKASSASJON + hoveddokument1.getDokumentInfoId(),
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AKSJON_ANGRE_LOGISK_TIDLIG_KASSASJON)),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		assertThat(begrensningRepository.count(), is(0L));
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
				URL_ANGRE_LOGISKTIDLIGKASSASJON + dokumentInfo.getDokumentInfoId(),
				HttpMethod.POST,
				createNoAccessHeaders(),
				String.class);


		assertThat(responseEntity.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
	}
}
