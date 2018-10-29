package no.nav.dokarkiv.fysiskslettdokument.rjoark102;

import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.DOKUMENT_INFO_ID_TEST;
import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.HJEMMEL;
import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.JOURNALPOST_ID_TEST;
import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.createRequest;
import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.knyttJournalpostSomVedleggTilJournalpostForEnhetstest;
import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.opprettDokumentForEnhetstest;
import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.opprettOgReturnerVedleggRelasjonForEnhetstest;

import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.DokumentIkkeLogiskSlettetException;
import no.nav.dokarkiv.core.exceptions.ForMangeJournalpostDokumentInfoRelasjonerException;
import no.nav.dokarkiv.core.exceptions.IngenRelasjonMellomJournalpostIdOgDokumentInfoIdException;
import no.nav.dokarkiv.core.exceptions.JournalpostDokumentInfoRelasjonIkkeFunnetException;
import no.nav.dokarkiv.fysiskslettdokument.exceptions.DokumentErIkkeHoveddokumentException;
import no.nav.dokarkiv.fysiskslettdokument.exceptions.DokumentErIkkeVedleggException;
import no.nav.dokarkiv.fysiskslettdokument.util.TestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit test for {@link FysiskSlettDokumentValidator}
 */
@RunWith(MockitoJUnitRunner.class)
public class FysiskSlettDokumentValidatorTest {

	@InjectMocks
	private FysiskSlettDokumentValidator validator;

	@Rule
	public ExpectedException thrown = ExpectedException.none();


	// fysiskSlettAvKunEttVedleggKnyttetJP ---------------------------------------------

	@Test
	public void shouldValidateFysiskSlettAvEtVedlegg() {
		FysiskSlettDokumentRequestTo requestTo = createRequest();
		JournalpostDokumentInfoRelasjon relasjonSomSkalSlettes = opprettOgReturnerVedleggRelasjonForEnhetstest(true);

		validator.validerFysiskSlettAvEtVedlegg(relasjonSomSkalSlettes, requestTo);
	}

	@Test
	public void shouldFailToValidateFysiskSlettAvEtVedleggBecauseDokumentErIkkeLogiskSlettet() {
		thrown.expect(DokumentIkkeLogiskSlettetException.class);
		thrown.expectMessage(String.format("%s kan ikke fysisk slette dokument som ikke er logisk slettet. dokumenInfoId=%s, journalpostId=%s",
				MDC.get(MDCConstants.MDC_REQUEST_ID),
				DOKUMENT_INFO_ID_TEST,
				JOURNALPOST_ID_TEST));

		FysiskSlettDokumentRequestTo requestTo = createRequest();
		JournalpostDokumentInfoRelasjon relasjonSomSkalSlettes = opprettOgReturnerVedleggRelasjonForEnhetstest(false);

		validator.validerFysiskSlettAvEtVedlegg(relasjonSomSkalSlettes, requestTo);
	}

	@Test
	public void shouldFailToValidateFysiskSlettAvEtVedleggBecauseDokumentErIkkeTilknyttetSomVedlegg() {
		thrown.expect(DokumentErIkkeVedleggException.class);
		thrown.expectMessage(String.format("%s kan ikke slette dokument som ikke er et vedlegg når hjemmel=%s er brukt. " +
						"dokumentInfoId=%s, journalpostId=%s",
				MDC.get(MDCConstants.MDC_REQUEST_ID),
				HJEMMEL,
				DOKUMENT_INFO_ID_TEST,
				JOURNALPOST_ID_TEST));

		FysiskSlettDokumentRequestTo requestTo = createRequest();
		Journalpost jpHoveddokument = opprettDokumentForEnhetstest(true);

		JournalpostDokumentInfoRelasjon relasjonSomSkalSlettes = jpHoveddokument.findHoveddokumentDokumentInfoRelasjon();

		validator.validerFysiskSlettAvEtVedlegg(relasjonSomSkalSlettes, requestTo);
	}

	// Her må sletteregler på plass, Disse antar at et vedlegg lever helt adskilt.

//	@Test
//	public void shouldValidateKunEtVedleggSkalSlettes() {
//		FysiskSlettDokumentRequestTo requestTo = createRequest();
//		JournalpostDokumentInfoRelasjon relasjonSomSkalSlettes = opprettOgReturnerVedleggRelasjonForEnhetstest(true);
//
//
//
//		Journalpost jpVedlegg = TestUtils.opprettDokumentForEnhetstest(true);
//		Journalpost jpHoveddokument = TestUtils.opprettDokumentForEnhetstest(false);
//		knyttJournalpostSomVedleggTilJournalpostForEnhetstest(jpVedlegg, jpHoveddokument);
//
//		List<JournalpostDokumentInfoRelasjon> listSomSkalSlettes =
//				new ArrayList<JournalpostDokumentInfoRelasjon>(jpHoveddokument.getJournalpostDokumentInfoRelasjoner());
//
//		validator.validerAtKunEtHoveddokumentSkalSlettes(listSomSkalSlettes, listSomSkalSlettes, requestTo);
//	}

//	@Test
//	public void shouldFailToValidateKunEtVedleggSkalSlettesBecauseJournalpostDokumentInfoRelasjonerFinnesIkke() {
//		thrown.expect(JournalpostDokumentInfoRelasjonIkkeFunnetException.class);
//		thrown.expectMessage(String.format("%s kan ikke finne journalpostDokumentInfoRelasjon med journalpostId=%s",
//				MDC.get(MDCConstants.MDC_REQUEST_ID),
//				JOURNALPOST_ID_TEST));
//
//		FysiskSlettDokumentRequestTo requestTo = createRequest();
//		Journalpost journalpostSomSkalSlettes = TestUtils.opprettDokumentForEnhetstest(true);
//
//		List<JournalpostDokumentInfoRelasjon> listSomSkalSlettes = new ArrayList<JournalpostDokumentInfoRelasjon>();
//
//		validator.validerAtKunEtHoveddokumentSkalSlettes(listSomSkalSlettes, listSomSkalSlettes, requestTo);
//	}
//

// 	@Test
//	public void shouldFailToValidateKunEtVedleggSkalSlettesBecauseJournalpostDokumentInfoRelasjonErKnyttetTilFlereRelasjoner() {
//		thrown.expect(ForMangeJournalpostDokumentInfoRelasjonerException.class);
//		thrown.expectMessage(String.format("%s kan ikke slette en journalpost som har relasjoner med flere dokumenter. " +
//						"JournalpostId=%s har relasjoner med %s dokumenter.",
//				MDC.get(MDCConstants.MDC_REQUEST_ID),
//				JOURNALPOST_ID_TEST,
//				2L));
//		FysiskSlettDokumentRequestTo requestTo = createRequest();
//		Journalpost jpHoveddokument = opprettDokumentForEnhetstest(true);
//		Journalpost jpVedlegg= opprettDokumentForEnhetstest(true);
//		knyttJournalpostSomVedleggTilJournalpostForEnhetstest(jpVedlegg, jpHoveddokument);
//
//		List<JournalpostDokumentInfoRelasjon> relasjonList =
//				new ArrayList<JournalpostDokumentInfoRelasjon>(jpHoveddokument.getJournalpostDokumentInfoRelasjoner());
//
//		validator.validerAtKunEtHoveddokumentSkalSlettes(relasjonList, relasjonList, requestTo);
//	}
//

//	@Test
//	public void shouldFailToValidateKunEtVedleggSkalSlettesBecauseInputPekerIkkePaaSammeRelasjon() {
//		thrown.expect(IngenRelasjonMellomJournalpostIdOgDokumentInfoIdException.class);
//		thrown.expectMessage(String.format("%s finner ingen journalpostDokumentInfoRelasjon mellom journalpostId=%s og dokumentInfoId=%s",
//				MDC.get(MDCConstants.MDC_REQUEST_ID),
//				JOURNALPOST_ID_TEST,
//				DOKUMENT_INFO_ID_TEST));
//		FysiskSlettDokumentRequestTo requestTo = createRequest();
//		Journalpost jpDokument1 = opprettDokumentForEnhetstest(true);
//		Journalpost jpDokument2 = opprettDokumentForEnhetstest(true);
//
//		List<JournalpostDokumentInfoRelasjon> relasjonList1 =
//				new ArrayList<JournalpostDokumentInfoRelasjon>(jpDokument1.getJournalpostDokumentInfoRelasjoner());
//		List<JournalpostDokumentInfoRelasjon> relasjonList2 =
//				new ArrayList<JournalpostDokumentInfoRelasjon>(jpDokument2.getJournalpostDokumentInfoRelasjoner());
//
//		validator.validerAtKunEtHoveddokumentSkalSlettes(relasjonList1, relasjonList2, requestTo);
//	}

	// FysiskSlettAvKunEttHoveddokumentKnyttetJP ---------------------------------------------
	@Test
	public void shouldValidateFysiskSlettAvEttHoveddokument() {
		FysiskSlettDokumentRequestTo requestTo = createRequest();
		Journalpost journalpostSomSkalSlettes = TestUtils.opprettDokumentForEnhetstest(true);

		JournalpostDokumentInfoRelasjon relasjonSomSkalSlettes = journalpostSomSkalSlettes.findHoveddokumentDokumentInfoRelasjon();

		validator.validerFysiskSlettAvEtHoveddokument(relasjonSomSkalSlettes, requestTo);
	}

	@Test
	public void shouldFailToValidateFysiskSlettAvEttHoveddokumentBecauseDokumentErIkkeLogiskSlettet() {
		thrown.expect(DokumentIkkeLogiskSlettetException.class);
		thrown.expectMessage(String.format("%s kan ikke fysisk slette dokument som ikke er logisk slettet. dokumenInfoId=%s, journalpostId=%s",
				MDC.get(MDCConstants.MDC_REQUEST_ID),
				DOKUMENT_INFO_ID_TEST,
				JOURNALPOST_ID_TEST));

		FysiskSlettDokumentRequestTo requestTo = createRequest();
		Journalpost journalpostSomSkalSlettes = TestUtils.opprettDokumentForEnhetstest(false);

		JournalpostDokumentInfoRelasjon relasjonSomSkalSlettes = journalpostSomSkalSlettes.findHoveddokumentDokumentInfoRelasjon();

		validator.validerFysiskSlettAvEtHoveddokument(relasjonSomSkalSlettes, requestTo);
	}

	@Test
	public void shouldFailToValidateFysiskSlettAvEttHoveddokumentBecauseDokumentErIkkeTilknyttetSomHoveddokument() {
		thrown.expect(DokumentErIkkeHoveddokumentException.class);
		String.format("%s kan ikke slette dokument som ikke er hoveddokument når hjemmel=%s er brukt. " +
						"dokumentInfoId=%s, journalpostId=%s",
				MDC.get(MDCConstants.MDC_REQUEST_ID),
				HJEMMEL,
				DOKUMENT_INFO_ID_TEST,
				JOURNALPOST_ID_TEST);

		FysiskSlettDokumentRequestTo requestTo = createRequest();
		JournalpostDokumentInfoRelasjon relasjonSomSkalSlettes = opprettOgReturnerVedleggRelasjonForEnhetstest(true);

		validator.validerFysiskSlettAvEtHoveddokument(relasjonSomSkalSlettes, requestTo);
	}

	@Test
	public void shouldValidateKunEtHoveddokumentSkalSlettes() {
		FysiskSlettDokumentRequestTo requestTo = createRequest();
		Journalpost journalpostSomSkalSlettes = TestUtils.opprettDokumentForEnhetstest(true);

		List<JournalpostDokumentInfoRelasjon> listSomSkalSlettes =
				new ArrayList<JournalpostDokumentInfoRelasjon>(journalpostSomSkalSlettes.getJournalpostDokumentInfoRelasjoner());

		validator.validerAtKunEtHoveddokumentSkalSlettes(listSomSkalSlettes, listSomSkalSlettes, requestTo);
	}

	@Test
	public void shouldFailToValidateKunEtHoveddokumentSkalSlettesBecauseJournalpostDokumentInfoRelasjonerFinnesIkke() {
		thrown.expect(JournalpostDokumentInfoRelasjonIkkeFunnetException.class);
		thrown.expectMessage(String.format("%s kan ikke finne journalpostDokumentInfoRelasjon med journalpostId=%s",
				MDC.get(MDCConstants.MDC_REQUEST_ID),
				JOURNALPOST_ID_TEST));

		FysiskSlettDokumentRequestTo requestTo = createRequest();
		Journalpost journalpostSomSkalSlettes = TestUtils.opprettDokumentForEnhetstest(true);

		List<JournalpostDokumentInfoRelasjon> listSomSkalSlettes = new ArrayList<JournalpostDokumentInfoRelasjon>();

		validator.validerAtKunEtHoveddokumentSkalSlettes(listSomSkalSlettes, listSomSkalSlettes, requestTo);
	}

	@Test
	public void shouldFailToValidateKunEtHoveddokumentSkalSlettesBecauseJournalpostDokumentInfoRelasjonErKnyttetTilFlereRelasjoner() {
		thrown.expect(ForMangeJournalpostDokumentInfoRelasjonerException.class);
		thrown.expectMessage(String.format("%s kan ikke slette en journalpost som har relasjoner med flere dokumenter. " +
						"JournalpostId=%s har relasjoner med %s dokumenter.",
				MDC.get(MDCConstants.MDC_REQUEST_ID),
				JOURNALPOST_ID_TEST,
				2L));
		FysiskSlettDokumentRequestTo requestTo = createRequest();
		Journalpost jpHoveddokument = opprettDokumentForEnhetstest(true);
		Journalpost jpVedlegg = opprettDokumentForEnhetstest(true);
		knyttJournalpostSomVedleggTilJournalpostForEnhetstest(jpVedlegg, jpHoveddokument);

		List<JournalpostDokumentInfoRelasjon> relasjonList =
				new ArrayList<JournalpostDokumentInfoRelasjon>(jpHoveddokument.getJournalpostDokumentInfoRelasjoner());

		validator.validerAtKunEtHoveddokumentSkalSlettes(relasjonList, relasjonList, requestTo);
	}

	@Test
	public void shouldFailToValidateKunEtHoveddokumentSkalSlettesBecauseInputPekerIkkePaaSammeRelasjon() {
		thrown.expect(IngenRelasjonMellomJournalpostIdOgDokumentInfoIdException.class);
		thrown.expectMessage(String.format("%s finner ingen journalpostDokumentInfoRelasjon mellom journalpostId=%s og dokumentInfoId=%s",
				MDC.get(MDCConstants.MDC_REQUEST_ID),
				JOURNALPOST_ID_TEST,
				DOKUMENT_INFO_ID_TEST));
		FysiskSlettDokumentRequestTo requestTo = createRequest();
		Journalpost jpDokument1 = opprettDokumentForEnhetstest(true);
		Journalpost jpDokument2 = opprettDokumentForEnhetstest(true);

		List<JournalpostDokumentInfoRelasjon> relasjonList1 =
				new ArrayList<JournalpostDokumentInfoRelasjon>(jpDokument1.getJournalpostDokumentInfoRelasjoner());
		List<JournalpostDokumentInfoRelasjon> relasjonList2 =
				new ArrayList<JournalpostDokumentInfoRelasjon>(jpDokument2.getJournalpostDokumentInfoRelasjoner());

		validator.validerAtKunEtHoveddokumentSkalSlettes(relasjonList1, relasjonList2, requestTo);
	}
}
