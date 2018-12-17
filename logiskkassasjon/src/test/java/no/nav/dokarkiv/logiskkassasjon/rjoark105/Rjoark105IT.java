package no.nav.dokarkiv.logiskkassasjon.rjoark105;

import static no.nav.dokarkiv.logiskkassasjon.util.TestUtils.kasserDokumentLogisk;
import static no.nav.dokarkiv.logiskkassasjon.util.TestUtils.opprettHoveddokumentForIT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.logiskkassasjon.AbstractLogiskKassasjonIT;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

public class Rjoark105IT extends AbstractLogiskKassasjonIT {

	@Test
	public void skallIkkeLogiskKassereDokument_ettersomDokumentInfoIdIkkeFinnes() {
		abacPermit();

		Long dokumentInfoId = 13L;

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_LOGISKKASSASJON + dokumentInfoId,
				HttpMethod.PATCH,
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

		begrensningRepository.save(kasserDokumentLogisk(dokumentInfo));

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_LOGISKKASSASJON + dokumentInfo.getDokumentInfoId(),
				HttpMethod.PATCH,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(String.format(
				"Kan ikke utføre logisk kassasjon av dokument med dokumentInfoId=%s. Dokumentet er allerede logisk kassert",
				dokumentInfo.getDokumentInfoId())));

	}

//	@Test
//	public void skallIkkeLogiskKassereDokument_ettersomJournalpostDokumentInfoRelasjonIkkeErKassert(){}


	//TODO: Avklare hva som skal skje når dokument er knyttet flere journalposter
	/**
	 @Test public void skallIkkeLogiskKassereDokument_ettersomDokumentErKnyttetFlereJournalposter(){
	 abacPermit();

	 Journalpost journalpost1 = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());
	 Journalpost journalpost2 = opprettHoveddokumentForIT();

	 DokumentInfo vedlegg = journalpost1.findDokumentInfoRelasjonByTilknyttetJournalpostSom(
	 TilknyttetJournalpostSomCode.VEDLEGG).iterator().next().getDokumentInfo();

	 knyttDokumentInfoSomVedleggTilJournalpostForIT(vedlegg, journalpost2);

	 joarkRepository.save(journalpost2);

	 TestTransaction.flagForCommit();
	 TestTransaction.end();

	 ResponseEntity<String> responseEntity = restTemplate.exchange(
	 URL_LOGISKKASSASJON + vedlegg.getDokumentInfoId(),
	 HttpMethod.PATCH,
	 createHeaders(),
	 String.class);

	 assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_IMPLEMENTED));
	 assertThat(responseEntity.getBody(), containsString(String.format(
	 "Kan ikke utføre tidlig kassasjon av dokument med dokumentInfoId=%s fordi dokumentet er knyttet til flere " +
	 "journalposter og den funksjonaliteten er ikke implementert", vedlegg.getDokumentInfoId())));
	 }

	 @Test public void skallLogiskKassereDokument(){
	 abacPermit();

	 Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());
	 DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();


	 TestTransaction.flagForCommit();
	 TestTransaction.end();

	 ResponseEntity<String> responseEntity = restTemplate.exchange(
	 URL_LOGISKKASSASJON + dokumentInfo.getDokumentInfoId(),
	 HttpMethod.PATCH,
	 createHeaders(),
	 String.class);

	 assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
	 }
	 **/
}
