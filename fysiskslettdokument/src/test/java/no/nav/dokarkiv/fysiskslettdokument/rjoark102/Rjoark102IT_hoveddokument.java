package no.nav.dokarkiv.fysiskslettdokument.rjoark102;

import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.HJEMMEL_HOVEDDOKUMENT;
import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.knyttDokumentInfoSomVedleggTilJournalpostForIT;
import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.opprettHoveddokumentForIT;
import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.oppretteDokumentOgKnyttVedleggForIt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
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

public class Rjoark102IT_hoveddokument extends AbstractFysiskSlettDokumentIT {

	@Inject
	private JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;

	// fysiskSlettEtHoveddokumentKnyttetEnJP ---------------------------------------------

	@Test
	public void shouldFysiskSlettEtHoveddokumentKnyttetEnJP() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT(true));

		DokumentInfo hoveddokument = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		TestTransaction.flagForCommit();
		TestTransaction.end();

		//TODO: Valider med responseEntity etter kvitteringsmelding er avklart
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKSLETTDOKUMENT + journalpost.getJournalpostId() + "/"
						+ hoveddokument.getDokumentInfoId() + "/" + HJEMMEL_HOVEDDOKUMENT,
				HttpMethod.DELETE,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		assertFalse(journalpostDokumentInfoRelasjonRepository.findAllByJournalpostJournalpostId(journalpost.getJournalpostId())
				.isPresent());
		assertFalse(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(hoveddokument.getDokumentInfoId())
				.isPresent());

		assertFalse(dokumentinfoRepository.findAllByOriginalJournalpostJournalpostIdAndDokumentInfoId(
				journalpost.getJournalpostId(), hoveddokument.getDokumentInfoId()).isPresent());

		assertTrue(joarkRepository.findAllJournalpostIdsByDokumentInfoId(hoveddokument.getDokumentInfoId()).isEmpty());
	}

	@Test
	public void shouldFailToFysiskSlettEtHoveddokumentKnyttetEnJPBecauseHoveddokumentErTilknyttetAnnenJPSomVedlegg() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT(true));
		Journalpost journalpostSomSkalSlettes = joarkRepository.save(opprettHoveddokumentForIT(true));

		DokumentInfo dokumentSomSkalSlettes = journalpostSomSkalSlettes.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo();

		knyttDokumentInfoSomVedleggTilJournalpostForIT(dokumentSomSkalSlettes, journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		//TODO: Valider med responseEntity etter kvitteringsmelding er avklart
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKSLETTDOKUMENT + journalpostSomSkalSlettes.getJournalpostId() + "/"
						+ dokumentSomSkalSlettes.getDokumentInfoId() + "/" + HJEMMEL_HOVEDDOKUMENT,
				HttpMethod.DELETE,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(
				String.format("%s kan ikke slette et dokument som er knyttet til flere journalposter. " +
								"dokumentInfoId=%s har relasjoner med %s journalposter.",
						MDC.get(MDCConstants.MDC_REQUEST_ID),
						dokumentSomSkalSlettes.getDokumentInfoId(),
						2)));
	}

	@Test
	public void shouldFysiskSlettEtHoveddokumentMedFlereVedleggKnyttetEnJP() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(oppretteDokumentOgKnyttVedleggForIt(true, 3));

		DokumentInfo hoveddokument = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		//sett alle vedlegg til sletting
		for (JournalpostDokumentInfoRelasjon vedleggRelasjon :
				journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)) {
			vedleggRelasjon.getDokumentInfo().setSlettet(true);
		}

		TestTransaction.flagForCommit();
		TestTransaction.end();

		assertEquals(4L, journalpostDokumentInfoRelasjonRepository
				.findAllByJournalpostJournalpostId(journalpost.getJournalpostId()).get().size());

		//TODO: Valider med responseEntity etter kvitteringsmelding er avklart
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKSLETTDOKUMENT + journalpost.getJournalpostId() + "/"
						+ hoveddokument.getDokumentInfoId() + "/" + HJEMMEL_HOVEDDOKUMENT,
				HttpMethod.DELETE,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		assertFalse(journalpostDokumentInfoRelasjonRepository.findAllByJournalpostJournalpostId(journalpost.getJournalpostId())
				.isPresent());
		assertFalse(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(hoveddokument.getDokumentInfoId())
				.isPresent());

		assertFalse(dokumentinfoRepository.findAllByOriginalJournalpostJournalpostIdAndDokumentInfoId(
				journalpost.getJournalpostId(), hoveddokument.getDokumentInfoId()).isPresent());

		assertTrue(joarkRepository.findAllJournalpostIdsByDokumentInfoId(hoveddokument.getDokumentInfoId()).isEmpty());
	}

	@Test
	public void shouldFailToFysiskSlettEtHoveddokumentMedFlereVedleggKnyttetEnJPBecauseVedleggErIkkeLogiskSlettet() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(oppretteDokumentOgKnyttVedleggForIt(true, 3));

		DokumentInfo hoveddokument = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		Set<JournalpostDokumentInfoRelasjon> setRelasjoner =
				journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG);

		ArrayList<JournalpostDokumentInfoRelasjon> jpDokInfoRelasjonList = new ArrayList<>(setRelasjoner);

		DokumentInfo vedlegg1 = jpDokInfoRelasjonList.get(0).getDokumentInfo();
		DokumentInfo vedlegg2 = jpDokInfoRelasjonList.get(1).getDokumentInfo();
		DokumentInfo vedlegg3 = jpDokInfoRelasjonList.get(2).getDokumentInfo();

		//Vedlegg2 skal ikke settes som logisk slettet, false er default
		vedlegg1.setSlettet(true);
		vedlegg3.setSlettet(true);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		assertEquals(4L, journalpostDokumentInfoRelasjonRepository.findAllByJournalpostJournalpostId(journalpost.getJournalpostId())
				.get()
				.size());

		//TODO: Valider med responseEntity etter kvitteringsmelding er avklart
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKSLETTDOKUMENT + journalpost.getJournalpostId() + "/"
						+ hoveddokument.getDokumentInfoId() + "/" + HJEMMEL_HOVEDDOKUMENT,
				HttpMethod.DELETE,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(
				String.format("%s kan ikke fysisk slette dokument som ikke er logisk slettet. dokumenInfoId=%s, journalpostId=%s",
						MDC.get(MDCConstants.MDC_REQUEST_ID),
						vedlegg2.getDokumentInfoId(),
						journalpost.getJournalpostId())));
	}

	@Test
	public void shouldFailToFysiskSlettEtHoveddokumentMedFlereVedleggKnyttetEnJPBecauseVedleggErKnyttetAnnenJPSomVedlegg() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(oppretteDokumentOgKnyttVedleggForIt(true, 3));
		Journalpost annenJPSomVedlegg2ErKnyttetTil = joarkRepository.save(opprettHoveddokumentForIT(false));

		DokumentInfo hoveddokument = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		Set<JournalpostDokumentInfoRelasjon> setRelasjoner =
				journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG);

		ArrayList<JournalpostDokumentInfoRelasjon> jpDokInfoRelasjonList = new ArrayList<>(setRelasjoner);

		DokumentInfo vedlegg1 = jpDokInfoRelasjonList.get(0).getDokumentInfo();
		DokumentInfo vedlegg2 = jpDokInfoRelasjonList.get(1).getDokumentInfo();
		DokumentInfo vedlegg3 = jpDokInfoRelasjonList.get(2).getDokumentInfo();

		vedlegg1.setSlettet(true);
		vedlegg2.setSlettet(true);
		vedlegg3.setSlettet(true);

		knyttDokumentInfoSomVedleggTilJournalpostForIT(vedlegg2, annenJPSomVedlegg2ErKnyttetTil);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		assertEquals(4L, journalpostDokumentInfoRelasjonRepository.findAllByJournalpostJournalpostId(
				journalpost.getJournalpostId()).get().size());

		//TODO: Valider med responseEntity etter kvitteringsmelding er avklart
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKSLETTDOKUMENT + journalpost.getJournalpostId() + "/"
						+ hoveddokument.getDokumentInfoId() + "/" + HJEMMEL_HOVEDDOKUMENT,
				HttpMethod.DELETE,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(
				String.format("%s kan ikke slette et dokument som er knyttet til flere journalposter. " +
								"dokumentInfoId=%s har relasjoner med %s journalposter.",
						MDC.get(MDCConstants.MDC_REQUEST_ID),
						vedlegg2.getDokumentInfoId(),
						2)));
	}
}
