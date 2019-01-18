package no.nav.dokarkiv.logisktidligkassasjon.rjoark105;

import static junit.framework.TestCase.assertTrue;
import static no.nav.dokarkiv.logisktidligkassasjon.util.TestUtils.kassereDokumentLogisk;
import static no.nav.dokarkiv.logisktidligkassasjon.util.TestUtils.knyttDokumentInfoSomVedleggTilJournalpostForIT;
import static no.nav.dokarkiv.logisktidligkassasjon.util.TestUtils.opprettHoveddokumentForIT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;

import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.logisktidligkassasjon.AbstractLogiskTidligKassasjonIT;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

public class Rjoark105IT extends AbstractLogiskTidligKassasjonIT {

	@Test
	public void skallIkkeLogiskTidligKassereDokument_ettersomDokumentInfoIdIkkeFinnes() {
		abacPermit();

		Long dokumentInfoId = 13L;

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_LOGISKTIDLIGKASSASJON + dokumentInfoId,
				HttpMethod.POST,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getBody(), containsString("DokumentInfo ikke funnet. dokumentInfoId=" + dokumentInfoId));
	}

	@Test
	public void skallIkkeLogiskTidligKassereDokument_ettersomDokumentErKassert() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		begrensningRepository.save(kassereDokumentLogisk(dokumentInfo));

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_LOGISKTIDLIGKASSASJON + dokumentInfo.getDokumentInfoId(),
				HttpMethod.POST,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(String.format(
				"Kan ikke utføre logisk kassasjon av dokument med dokumentInfoId=%s. Dokumentet er allerede logisk kassert",
				dokumentInfo.getDokumentInfoId())));
	}

	@Test
	public void skallLogiskTidligKassereDokument_medDokumentKnyttetFlereJournalposter() {
		abacPermit();
		Journalpost journalpost1 = joarkRepository.save(opprettHoveddokumentForIT());
		Journalpost journalpost2 = opprettHoveddokumentForIT();

		DokumentInfo hoveddokument1 = journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		knyttDokumentInfoSomVedleggTilJournalpostForIT(hoveddokument1, journalpost2);

		joarkRepository.save(journalpost2);
		assertTrue(hoveddokument1.isRelatedToMultipleJournalposts());

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_LOGISKTIDLIGKASSASJON + hoveddokument1.getDokumentInfoId(),
				HttpMethod.POST,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		assertThat(begrensningRepository.count(), is(1L));
		assertTrue(begrensningRepository.findByDokumentInfoIdAndBegrensningType(hoveddokument1.getDokumentInfoId(), SkjermingTypeCode.POL)
				.isPresent());
	}

	@Test
	public void skallLogiskTidligKassereDokument_medDokumentKnyttetEnJournalpost() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		TestTransaction.flagForCommit();
		TestTransaction.end();

		assertThat(begrensningRepository.count(), is(0L));
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_LOGISKTIDLIGKASSASJON + dokumentInfo.getDokumentInfoId(),
				HttpMethod.POST,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		assertThat(begrensningRepository.count(), is(1L));
		assertTrue(begrensningRepository.findByDokumentInfoIdAndBegrensningType(dokumentInfo.getDokumentInfoId(), SkjermingTypeCode.POL)
				.isPresent());
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
				URL_LOGISKTIDLIGKASSASJON + dokumentInfo.getDokumentInfoId(),
				HttpMethod.POST,
				createNoAccessHeaders(),
				String.class);


		assertThat(responseEntity.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
	}
}
