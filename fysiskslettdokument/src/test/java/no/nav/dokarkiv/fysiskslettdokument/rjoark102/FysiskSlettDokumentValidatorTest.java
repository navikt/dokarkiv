package no.nav.dokarkiv.fysiskslettdokument.rjoark102;

import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.DOKUMENT_INFO_ID;
import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.HJEMMEL;
import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.JOURNALPOST_ID;

import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.JournalpostDokumentInfoRelasjonIkkeFunnetException;
import no.nav.dokarkiv.fysiskslettdokument.exceptions.DokumentErIkkeVedleggException;
import no.nav.dokarkiv.fysiskslettdokument.exceptions.DokumentIkkeLogiskSlettetException;
import no.nav.dokarkiv.fysiskslettdokument.util.TestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.MDC;

/**
 * Unit test for {@link FysiskSlettDokumentValidator}
 */
@RunWith(MockitoJUnitRunner.class)
public class FysiskSlettDokumentValidatorTest {

	@InjectMocks
	private FysiskSlettDokumentValidator validator;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void shouldValidateFysiskSlettAvKunEttVedleggKnyttetJP() {
		FysiskSlettDokumentRequestTo requestTo = TestUtils.createRequest();
		Journalpost journalpost = TestUtils.opprettVedleggForEnhetsTest(true);

		JournalpostDokumentInfoRelasjon jpDokInfoRel =
				journalpost.getThisJournalpostDokumentInfoRelasjon();

		validator.validerAtKunEtVedleggSkalSlettes(jpDokInfoRel, requestTo);
	}

	@Test
	public void shouldFailToValidateFysiskSlettAvKunEttVedleggBecauseJournalpostDokumentInfoRelasjonFinnesIkke() {
		thrown.expect(JournalpostDokumentInfoRelasjonIkkeFunnetException.class);
		thrown.expectMessage(String.format("%s kan ikke finne journalpostDokumentInfoRelasjon med journalpostId=%s",
				MDC.get(MDCConstants.MDC_REQUEST_ID), JOURNALPOST_ID));

		FysiskSlettDokumentRequestTo requestTo = TestUtils.createRequest();

		JournalpostDokumentInfoRelasjon jpDokInfoRel = null;

		validator.validerAtKunEtVedleggSkalSlettes(jpDokInfoRel, requestTo);
	}

	@Test
	public void shouldFailToValidateFysiskSlettAvKunEttVedleggBecauseDokumentErIkkeLogiskSlettet() {
		thrown.expect(DokumentIkkeLogiskSlettetException.class);
		thrown.expectMessage(String.format("%s kan ikke fysisk slette dokument som ikke er logisk slettet. dokumenInfoId=%s, journalpostId=%s",
				MDC.get(MDCConstants.MDC_REQUEST_ID),
				DOKUMENT_INFO_ID,
				JOURNALPOST_ID));

		FysiskSlettDokumentRequestTo requestTo = TestUtils.createRequest();
		Journalpost journalpost = TestUtils.opprettVedleggForEnhetsTest(false);

		JournalpostDokumentInfoRelasjon jpDokInfoRel = journalpost.getThisJournalpostDokumentInfoRelasjon();

		validator.validerAtKunEtVedleggSkalSlettes(jpDokInfoRel, requestTo);
	}

	@Test
	public void shouldFailToValidateFysiskSlettAvKunEttVedleggBecauseDokumentErIkkeTilknyttetSomVedlegg() {
		thrown.expect(DokumentErIkkeVedleggException.class);
		String.format("%s kan ikke slette dokument som ikke er et vedlegg når hjemmel=%s er brukt. " +
						"dokumentInfoId=%s, journalpostId=%s",
				MDC.get(MDCConstants.MDC_REQUEST_ID),
				HJEMMEL,
				DOKUMENT_INFO_ID,
				JOURNALPOST_ID);

		FysiskSlettDokumentRequestTo requestTo = TestUtils.createRequest();
		Journalpost journalpost = TestUtils.opprettHoveddokumentForEnhetstest(true);

		JournalpostDokumentInfoRelasjon jpDokInfoRel = journalpost.getThisJournalpostDokumentInfoRelasjon();

		validator.validerAtKunEtVedleggSkalSlettes(jpDokInfoRel, requestTo);
	}

}
