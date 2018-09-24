package no.nav.dokarkiv.logiskslettdokument.rjoark101;

import static no.nav.dokarkiv.logiskslettdokument.LogiskSlettDokumentRestController.REQUEST_ID;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.DOKUMENTINFO_ID;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.JOURNALPOST_ID;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.createDokumentInfo;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.createJournalpost;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.createRequest;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.logiskslettdokument.exceptions.DokumentIkkeSlettetException;
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
		Journalpost journalpost = createJournalpost(true);

		List<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjonList = new ArrayList<JournalpostDokumentInfoRelasjon>();
		journalpostDokumentInfoRelasjonList.addAll(journalpost.getJournalpostDokumentInfoRelasjoner());

		validator.validateAngreLogiskSlettDokument(journalpostDokumentInfoRelasjonList, requestTo);
	}

	@Test
	public void shouldValidateSletteStatusForDokument() throws Exception {
		validator.validateDokumentErLogiskSlettet(createDokumentInfo(true));
	}

	@Test
	public void shouldFailToValidateSletteStatusForDokument() {
		thrown.expect(DokumentIkkeSlettetException.class);
		thrown.expectMessage(REQUEST_ID + " prøver å angre logisk sletting av et dokument som ikke er logisk slettet, dokumentInfoId=" + DOKUMENTINFO_ID);

		validator.validateDokumentErLogiskSlettet(createDokumentInfo(false));
	}


}
