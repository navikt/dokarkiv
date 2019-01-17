package no.nav.dokarkiv.logiskkassasjon.rjoark105;

import static junit.framework.TestCase.assertTrue;
import static no.nav.dokarkiv.logiskkassasjon.util.TestUtils.knyttDokumentInfoSomVedleggTilJournalpostForIT;
import static no.nav.dokarkiv.logiskkassasjon.util.TestUtils.opprettHoveddokumentForIT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;

import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.logiskkassasjon.AbstractLogiskKassasjonIT;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.util.Optional;

public class Rjoark105IT extends AbstractLogiskKassasjonIT {

	@Test
	public void skallIkkeLogiskKassereDokument_ettersomDokumentInfoIdIkkeFinnes() {
		abacPermit();

		Long dokumentInfoId = 13L;

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_LOGISKKASSASJON + dokumentInfoId,
				HttpMethod.POST,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getBody(), containsString("DokumentInfo ikke funnet. dokumentInfoId=" + dokumentInfoId));
	}

	@Test
	public void skallIkkeLogiskKassereDokument_ettersomDokumentErKassert() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		begrensningService.setDokumentKassert(dokumentInfo, BegrensningTypeCode.POL);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_LOGISKKASSASJON + dokumentInfo.getDokumentInfoId(),
				HttpMethod.POST,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(String.format(
				"Kan ikke utføre logisk kassasjon av dokument med dokumentInfoId=%s. Dokumentet er allerede logisk kassert",
				dokumentInfo.getDokumentInfoId())));
	}

	@Test
	public void skallLogiskKassereDokument_medDokumentKnyttetFlereJournalposter() {
		abacPermit();
		Journalpost journalpost1 = joarkRepository.save(opprettHoveddokumentForIT());
		Journalpost journalpost2 = opprettHoveddokumentForIT();

		DokumentInfo hoveddokument1 = journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		knyttDokumentInfoSomVedleggTilJournalpostForIT(hoveddokument1, journalpost2);

		joarkRepository.save(journalpost2);
		assertTrue(hoveddokument1.isRelatedToMultipleJournalposts());

		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_LOGISKKASSASJON + hoveddokument1.getDokumentInfoId(),
				HttpMethod.POST,
				createHeaders(),
				String.class);

		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		Optional<DokumentInfo> dokumentInfo = dokumentinfoRepository.findByDokumentInfoId(hoveddokument1.getDokumentInfoId());
		assertTrue(dokumentInfo.isPresent());
		assertTrue(begrensningService.isDokumentInfoKassert(dokumentInfo.get()));
	}

	@Test
	public void skallLogiskKassereDokument_medDokumentKnyttetEnJournalpost() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();

		assertFalse(begrensningService.isDokumentInfoKassert(dokumentInfo));
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_LOGISKKASSASJON + dokumentInfo.getDokumentInfoId(),
				HttpMethod.POST,
				createHeaders(),
				String.class);

		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		Optional<DokumentInfo> dokumentInfoRep = dokumentinfoRepository.findByDokumentInfoId(dokumentInfo.getDokumentInfoId());
		assertTrue(dokumentInfoRep.isPresent());
		assertTrue(begrensningService.isDokumentInfoKassert(dokumentInfoRep.get()));
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
				URL_LOGISKKASSASJON + dokumentInfo.getDokumentInfoId(),
				HttpMethod.POST,
				createNoAccessHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
	}
}
