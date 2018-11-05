package no.nav.dokarkiv.fysiskslettdokument.rjoark102;

import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.DOKUMENT_INFO_ID_TEST;
import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.DOKUMENT_INFO_ID_TEST_VEDLEGG;
import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.HJEMMEL;
import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.JOURNALPOST_ID_TEST;
import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.TILKNYTTET_AV_NAVN;
import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.createRequest;
import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.opprettOgReturnerHoveddokumentRelasjonForEnhetstest;
import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.opprettOgReturnerVedleggRelasjonForEnhetstest;
import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.resetIds;

import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.DokumentIkkeLogiskSlettetException;
import no.nav.dokarkiv.core.exceptions.ForMangeJournalpostDokumentInfoRelasjonerException;
import no.nav.dokarkiv.core.exceptions.IngenRelasjonMellomJournalpostIdOgDokumentInfoIdException;
import no.nav.dokarkiv.core.exceptions.JournalpostDokumentInfoRelasjonIkkeFunnetException;
import no.nav.dokarkiv.fysiskslettdokument.exceptions.DokumentErIkkeHoveddokumentException;
import no.nav.dokarkiv.fysiskslettdokument.exceptions.DokumentErIkkeVedleggException;
import org.junit.Before;
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

	@Before
	public void reset() {
		resetIds();
	}

	// validerFysiskSlettEtVedleggKnyttetEnJP ---------------------------------------------

	@Test
	public void shouldValiderFysiskSlettEtVedleggKnyttetEnJP() {
		JournalpostDokumentInfoRelasjon vedleggRelasjon = opprettOgReturnerVedleggRelasjonForEnhetstest(true);

		FysiskSlettDokumentRequestTo requestTo = createRequest(vedleggRelasjon);

		List<JournalpostDokumentInfoRelasjon> relasjonList = new ArrayList<>();
		relasjonList.add(vedleggRelasjon);

		validator.validerFysiskSlettEtVedleggKnyttetEnJP(relasjonList, requestTo);
	}

	@Test
	public void shouldFailToValiderFysiskSlettAvEtVedleggBecauseDokumentErIkkeTilknyttetSomVedlegg() {
		thrown.expect(DokumentErIkkeVedleggException.class);
		thrown.expectMessage(String.format("%s kan ikke slette dokument som ikke er et vedlegg når hjemmel=%s er brukt. " +
						"dokumentInfoId=%s, journalpostId=%s",
				MDC.get(MDCConstants.MDC_REQUEST_ID),
				HJEMMEL,
				DOKUMENT_INFO_ID_TEST,
				JOURNALPOST_ID_TEST));
		FysiskSlettDokumentRequestTo requestTo = createRequest();
		JournalpostDokumentInfoRelasjon hoveddokumentRelasjon = opprettOgReturnerHoveddokumentRelasjonForEnhetstest(true);

		List<JournalpostDokumentInfoRelasjon> relasjonList = new ArrayList<>();
		relasjonList.add(hoveddokumentRelasjon);

		validator.validerFysiskSlettEtVedleggKnyttetEnJP(relasjonList, requestTo);
	}

	// validerKunEnGyldigRelasjonFoundByDokumentInfoId

	@Test
	public void shouldFailToValiderFysiskSlettEtDokumentKnyttetEnJPBecauseJournalpostDokumentInfoRelasjonerFinnesIkke() {
		thrown.expect(JournalpostDokumentInfoRelasjonIkkeFunnetException.class);
		thrown.expectMessage(String.format("%s kan ikke finne journalpostDokumentInfoRelasjon med dokumentInfoId=%s",
				MDC.get(MDCConstants.MDC_REQUEST_ID),
				DOKUMENT_INFO_ID_TEST));
		FysiskSlettDokumentRequestTo requestTo = createRequest();

		List<JournalpostDokumentInfoRelasjon> relasjonList = new ArrayList<>();

		validator.validerFysiskSlettEtVedleggKnyttetEnJP(relasjonList, requestTo);
	}

	@Test
	public void shouldFailToValiderFysiskSlettEtDokumentKnyttetEnJPBecauseJournalpostDokumentInfoRelasjonerErKnyttetTilFlereJournalposter() {
		thrown.expect(ForMangeJournalpostDokumentInfoRelasjonerException.class);
		thrown.expectMessage(String.format("%s kan ikke slette et dokument som er knyttet til flere journalposter. " +
						"dokumentInfoId=%s har relasjoner med %s journalposter.",
				MDC.get(MDCConstants.MDC_REQUEST_ID),
				DOKUMENT_INFO_ID_TEST_VEDLEGG,
				2L));

		JournalpostDokumentInfoRelasjon vedleggRelasjon1 = opprettOgReturnerVedleggRelasjonForEnhetstest(true);
		JournalpostDokumentInfoRelasjon vedleggRelasjon2 = opprettOgReturnerVedleggRelasjonForEnhetstest(true);

		JournalpostDokumentInfoRelasjon failVedleggRelasjon =
				JournalpostDokumentInfoRelasjon.builder()
						.journalpostDokumentInfoRelasjonId(5L)
						.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
						.tilknyttetAvNavn(TILKNYTTET_AV_NAVN)
						.journalpost(vedleggRelasjon2.getJournalpost())
						.dokumentInfo(vedleggRelasjon1.getDokumentInfo())
						.build();

		FysiskSlettDokumentRequestTo requestTo = createRequest(failVedleggRelasjon);

		List<JournalpostDokumentInfoRelasjon> relasjonList = new ArrayList<>();
		relasjonList.add(vedleggRelasjon1);
		relasjonList.add(failVedleggRelasjon);

		validator.validerFysiskSlettEtVedleggKnyttetEnJP(relasjonList, requestTo);
	}

	@Test
	public void shouldFailToValiderFysiskSlettEtDokumentKnyttetEnJPBecauseFinnesIkkeRelasjonMellomInputParametere() {
		thrown.expect(IngenRelasjonMellomJournalpostIdOgDokumentInfoIdException.class);
		thrown.expectMessage(String.format("%s finner ingen journalpostDokumentInfoRelasjon mellom journalpostId=%s og dokumentInfoId=%s",
				MDC.get(MDCConstants.MDC_REQUEST_ID),
				JOURNALPOST_ID_TEST + 10,
				DOKUMENT_INFO_ID_TEST_VEDLEGG));
		JournalpostDokumentInfoRelasjon vedleggRelasjon = opprettOgReturnerVedleggRelasjonForEnhetstest(true);

		FysiskSlettDokumentRequestTo requestMedFeilJP = createRequest(JOURNALPOST_ID_TEST + 10, vedleggRelasjon.getDokumentInfo()
				.getDokumentInfoId());

		List<JournalpostDokumentInfoRelasjon> relasjonList = new ArrayList<>();
		relasjonList.add(vedleggRelasjon);

		validator.validerFysiskSlettEtVedleggKnyttetEnJP(relasjonList, requestMedFeilJP);
	}

	@Test
	public void shouldFailToValiderFysiskSlettEtDokumentKnyttetEnJPBecauseDokumentErIkkeLogiskSlettet() {
		thrown.expect(DokumentIkkeLogiskSlettetException.class);
		thrown.expectMessage(String.format("%s kan ikke fysisk slette dokument som ikke er logisk slettet. dokumenInfoId=%s, journalpostId=%s",
				MDC.get(MDCConstants.MDC_REQUEST_ID),
				DOKUMENT_INFO_ID_TEST_VEDLEGG,
				JOURNALPOST_ID_TEST));
		JournalpostDokumentInfoRelasjon vedleggRelasjon = opprettOgReturnerVedleggRelasjonForEnhetstest(false);

		FysiskSlettDokumentRequestTo requestTo = createRequest(vedleggRelasjon);

		List<JournalpostDokumentInfoRelasjon> relasjonList = new ArrayList<>();
		relasjonList.add(vedleggRelasjon);

		validator.validerFysiskSlettEtVedleggKnyttetEnJP(relasjonList, requestTo);
	}

	// validerFysiskSlettEtHoveddokumentKnyttetEnJP ---------------------------------------------

	@Test
	public void shouldValiderFysiskSlettEtHoveddokumentKnyttetEnJP() {
		FysiskSlettDokumentRequestTo requestTo = createRequest();
		JournalpostDokumentInfoRelasjon hoveddokumentRelasjon = opprettOgReturnerHoveddokumentRelasjonForEnhetstest(true);

		List<JournalpostDokumentInfoRelasjon> relasjonList = new ArrayList<>();
		relasjonList.add(hoveddokumentRelasjon);

		validator.validerFysiskSlettEtHoveddokumentKnyttetEnJP(relasjonList, requestTo);
	}

	@Test
	public void shouldFailToValiderFysiskSlettAvEtHoveddokumentBecauseDokumentErIkkeTilknyttetSomHoveddokument() {
		thrown.expect(DokumentErIkkeHoveddokumentException.class);
		thrown.expectMessage(String.format("%s kan ikke slette dokument som ikke er hoveddokument når hjemmel=%s er brukt. " +
						"dokumentInfoId=%s, journalpostId=%s",
				MDC.get(MDCConstants.MDC_REQUEST_ID),
				HJEMMEL,
				DOKUMENT_INFO_ID_TEST,
				JOURNALPOST_ID_TEST));
		FysiskSlettDokumentRequestTo requestTo = createRequest();
		JournalpostDokumentInfoRelasjon vedleggRelasjon = opprettOgReturnerVedleggRelasjonForEnhetstest(true);

		List<JournalpostDokumentInfoRelasjon> relasjonList = new ArrayList<>();
		relasjonList.add(vedleggRelasjon);

		validator.validerFysiskSlettEtHoveddokumentKnyttetEnJP(relasjonList, requestTo);
	}
}
