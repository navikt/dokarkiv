package no.nav.dokarkiv.logiskslettdokument;

import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.DOKUMENTINFO_ID;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.JOURNALPOST_ID;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.createJournalpost;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.createRequest;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.JournalpostDokumentInfoRelasjonIkkeFunnetException;
import no.nav.dokarkiv.logiskslettdokument.rjoark100.LogiskSlettDokumentRequestTo;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit test for {@link LogiskSlettDokumentValidator}
 */
@RunWith(MockitoJUnitRunner.class)
public class LogiskSlettDokumentValidatorTest {

	@InjectMocks
	private LogiskSlettDokumentValidator validator;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void validerAtJournalpostDokumentInfoRelasjonerFinnes_derRelasjonFinnes_skalValidereOK() {
		LogiskSlettDokumentRequestTo requestTo = createRequest(JOURNALPOST_ID, DOKUMENTINFO_ID);
		Journalpost journalpost = createJournalpost(DOKUMENTINFO_ID);
		JournalpostDokumentInfoRelasjon jpDokInfoRelasjoner = journalpost.findHoveddokumentDokumentInfoRelasjon();

		validator.validerAtJournalpostDokumentInfoRelasjonerFinnes(jpDokInfoRelasjoner, requestTo);
	}

	@Test
	public void validerAtJournalpostDokumentInfoRelasjonerFinnes_derRelasjonMangler_skalKasteJournalpostDokumentInfoRelasjonIkkeFunnetException() {
		thrown.expect(JournalpostDokumentInfoRelasjonIkkeFunnetException.class);
		thrown.expectMessage(String.format("Kan ikke finne noen relasjon mellom journalpost med journalpostId=%s og dokument " +
						"med dokumentInfoId=%s",
				JOURNALPOST_ID,
				DOKUMENTINFO_ID));

		LogiskSlettDokumentRequestTo requestTo = createRequest(JOURNALPOST_ID, DOKUMENTINFO_ID);
		JournalpostDokumentInfoRelasjon jpDokInfoRelasjoner = null;

		validator.validerAtJournalpostDokumentInfoRelasjonerFinnes(jpDokInfoRelasjoner, requestTo);
	}
}