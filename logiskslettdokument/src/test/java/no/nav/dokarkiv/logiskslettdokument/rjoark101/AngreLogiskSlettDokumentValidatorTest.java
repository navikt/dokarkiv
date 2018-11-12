package no.nav.dokarkiv.logiskslettdokument.rjoark101;

import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.DOKUMENTINFO_ID;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.JOURNALPOST_ID;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.createJournalpost;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.createRequest;

import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.entities.Begrensning;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.DokumentIkkeLogiskSlettetException;
import no.nav.dokarkiv.logiskslettdokument.rjoark100.LogiskSlettDokumentRequestTo;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit test for {@link AngreLogiskSlettDokumentValidator}
 */
@RunWith(MockitoJUnitRunner.class)
public class AngreLogiskSlettDokumentValidatorTest {

	@InjectMocks
	private AngreLogiskSlettDokumentValidator validator;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void shouldValidateAngreLogiskSlettDokument() throws Exception {
		LogiskSlettDokumentRequestTo requestTo = createRequest(JOURNALPOST_ID, DOKUMENTINFO_ID);
		Journalpost journalpost = createJournalpost(DOKUMENTINFO_ID);
		Begrensning begrensning = Begrensning.builder().journalpost(journalpost).begrensningType(BegrensningTypeCode.UTILGJENGELIGGJORT).build();
		begrensning.setOpprettetKildeNavn("Opprettet kilde");
//		journalpost.addBegrensning(begrensning);

		List<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjonList = new ArrayList<JournalpostDokumentInfoRelasjon>();
		journalpostDokumentInfoRelasjonList.addAll(journalpost.getJournalpostDokumentInfoRelasjoner());

		validator.validerAngreLogiskSlettAvEttDokument(journalpostDokumentInfoRelasjonList, requestTo);
	}


	@Test
	public void shouldFailToValidateSletteStatusForDokument() {
		thrown.expect(DokumentIkkeLogiskSlettetException.class);
		thrown.expectMessage("Kan ikke angre logisk sletting av journalpost med journalpostId=" + JOURNALPOST_ID + ". " +
				"Journalposten er ikke logisk slettet");

		LogiskSlettDokumentRequestTo requestTo = createRequest(JOURNALPOST_ID, DOKUMENTINFO_ID);
		Journalpost journalpost = createJournalpost(DOKUMENTINFO_ID);

		List<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjonList = new ArrayList<JournalpostDokumentInfoRelasjon>();
		journalpostDokumentInfoRelasjonList.addAll(journalpost.getJournalpostDokumentInfoRelasjoner());

		validator.validerAngreLogiskSlettAvEttDokument(journalpostDokumentInfoRelasjonList, requestTo);
	}


}
