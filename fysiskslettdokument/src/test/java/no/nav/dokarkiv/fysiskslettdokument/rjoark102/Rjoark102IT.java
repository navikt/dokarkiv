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

	// fysiskSlettEtVedleggKnyttetEnJP ---------------------------------------------

	@Test
	public void shouldFysiskSlettEtVedleggKnyttetEnJP() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(oppretteDokumentMedEtVedleggForIT(false, true));

		TestTransaction.flagForCommit();
		TestTransaction.end();

		DokumentInfo dokumentInfoHoveddokument = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		DokumentInfo dokInfoVedlegg = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(
				TilknyttetJournalpostSomCode.VEDLEGG).iterator().next().getDokumentInfo();

		FysiskSlettDokumentRequestTo requestTo =
				createRequest(journalpost.getJournalpostId(), dokInfoVedlegg.getDokumentInfoId(), HJEMMEL_VEDLEGG);

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

		assertTrue(dokumentinfoRepository.findDokumentInfoByJournalpostIdAndDokumentInfoId(
				journalpost.getJournalpostId(), dokumentInfoHoveddokument.getDokumentInfoId()).isPresent());
		assertFalse(dokumentinfoRepository.findDokumentInfoByJournalpostIdAndDokumentInfoId(
				journalpost.getJournalpostId(), dokInfoVedlegg.getDokumentInfoId()).isPresent());

		assertTrue(joarkRepository.findAllJournalpostIdsByDokumentInfoId(
				dokumentInfoHoveddokument.getDokumentInfoId()).size() > 0);
		assertFalse(joarkRepository.findAllJournalpostIdsByDokumentInfoId(
				dokInfoVedlegg.getDokumentInfoId()).size() > 0);

	}

	@Test
	public void shouldFailToFysiskSlettEtVedleggKnyttetEnJPBecauseVedleggKnyttetTilFlereJournalposter() {
		abacPermit();

		Journalpost journalpost1 = joarkRepository.save(oppretteDokumentMedEtVedleggForIT(false, true));
		Journalpost journalpost2 = joarkRepository.save(opprettHoveddokumentForIT(false));

		DokumentInfo vedlegg = journalpost1.findDokumentInfoRelasjonByTilknyttetJournalpostSom(
				TilknyttetJournalpostSomCode.VEDLEGG).iterator().next().getDokumentInfo();

		knyttDokumentInfoSomVedleggTilJournalpostForIT(vedlegg, journalpost2);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		FysiskSlettDokumentRequestTo requestTo =
				createRequest(journalpost1.getJournalpostId(), vedlegg.getDokumentInfoId(), HJEMMEL_VEDLEGG);

		//TODO: Valider med responseEntity etter kvitteringsmelding er avklart
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKSLETTDOKUMENT + journalpost1.getJournalpostId() + "/"
						+ vedlegg.getDokumentInfoId() + "/" + HJEMMEL_VEDLEGG,
				HttpMethod.DELETE,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(
				String.format("%s kan ikke slette et dokument som er knyttet til flere journalposter. " +
								"dokumentInfoId=%s har relasjoner med %s journalposter.",
						MDC.get(MDCConstants.MDC_REQUEST_ID),
						vedlegg.getDokumentInfoId(),
						2)));
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

		FysiskSlettDokumentRequestTo requestTo =
				createRequest(journalpost.getJournalpostId(), vedlegg2.getDokumentInfoId(), HJEMMEL_VEDLEGG);

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

		assertTrue(dokumentinfoRepository.findDokumentInfoByJournalpostIdAndDokumentInfoId(
				journalpost.getJournalpostId(), dokumentInfoHoveddokument.getDokumentInfoId()).isPresent());
		assertTrue(dokumentinfoRepository.findDokumentInfoByJournalpostIdAndDokumentInfoId(
				journalpost.getJournalpostId(), vedlegg1.getDokumentInfoId()).isPresent());
		assertFalse(dokumentinfoRepository.findDokumentInfoByJournalpostIdAndDokumentInfoId(
				journalpost.getJournalpostId(), vedlegg2.getDokumentInfoId()).isPresent());
		assertTrue(dokumentinfoRepository.findDokumentInfoByJournalpostIdAndDokumentInfoId(
				journalpost.getJournalpostId(), vedlegg3.getDokumentInfoId()).isPresent());

		assertTrue(joarkRepository.findAllJournalpostIdsByDokumentInfoId(
				dokumentInfoHoveddokument.getDokumentInfoId()).size() > 0);
		assertTrue(joarkRepository.findAllJournalpostIdsByDokumentInfoId(vedlegg1.getDokumentInfoId()).size() > 0);
		assertFalse(joarkRepository.findAllJournalpostIdsByDokumentInfoId(vedlegg2.getDokumentInfoId()).size() > 0);
		assertTrue(joarkRepository.findAllJournalpostIdsByDokumentInfoId(vedlegg3.getDokumentInfoId()).size() > 0);
	}
}
