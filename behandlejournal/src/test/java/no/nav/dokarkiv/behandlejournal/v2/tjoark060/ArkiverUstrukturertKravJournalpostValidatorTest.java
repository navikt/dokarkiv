package no.nav.dokarkiv.behandlejournal.v2.tjoark060;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.behandlejournal.v2.AbstractBehandleJournalJournalpostValidatorTest;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.exceptions.InvalidJournalpostStructureException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests of ArkiverUstrukturertKravJournalpostValidator.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
@RunWith(MockitoJUnitRunner.class)
public class ArkiverUstrukturertKravJournalpostValidatorTest extends AbstractBehandleJournalJournalpostValidatorTest {

	private static final String MOTTAKER = "mottaker";
	private static final String AVSENDER_MOTTAKER_ID = "12345542";
	@InjectMocks
	private ArkiverUstrukturertKravJournalpostValidator validator;

	@Test
	public void shouldThrowExceptionIfFagomradeNotSetOnJournalpost() {
		journalpost.setFagomrade(null);
		validateAndAssertExceptionThrownWithMessage(validator, ApplicationException.class, "Fagomrade must be set");
	}

	@Test
	public void shouldThrowExceptionIfMottattDatoNotSetOnJournalpost() {
		journalpost.setMottattDato(null);
		validateAndAssertExceptionThrownWithMessage(validator, ApplicationException.class, "MottattDato must be set");
	}

	@Test
	public void shouldThrowExceptionIfMottakskanalNotSetOnJournalpost() {
		journalpost.setMottakskanal(null);
		validateAndAssertExceptionThrownWithMessage(validator, ApplicationException.class, "Mottakskanal must be set");
	}

	@Test
	public void shouldThrowExceptionIfSignaturNotSetOnJournalpost() {
		journalpost.setSignatur(null);
		validateAndAssertExceptionThrownWithMessage(validator, ApplicationException.class, "Signatur must be set");
	}

	@Test
	public void shouldThrowExceptionIfNoBrukereOnJournalpost() {
		journalpost.clearBrukere();
		validateAndAssertExceptionThrownWithMessage(validator, ApplicationException.class, "Journalpost must have a bruker");
	}

	@Test
	public void shouldThrowExceptionIfNoFildetaljerOnJournalpostOnJournalpost() {
		journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().clearFildetaljerListe();
		validateAndAssertExceptionThrownWithMessage(validator, ApplicationException.class, "FilDetaljer must be set");
	}

	@Test
	public void shouldThrowExcpetionIfNoFileContentOnFildetaljerOnJournalpost() {
		journalpost.findAllFilDetaljer().get(0).setFileContent(null);
		validateAndAssertExceptionThrownWithMessage(validator, ApplicationException.class, "FilDetaljer must have filecontent");
	}

	@Test
	public void shouldThrowExceptionIfDokumentDuplicatesOnJournalpost() {
		addDuplicatesOfVariantFormats(journalpost);
		validateAndAssertExceptionThrownWithMessage(validator, InvalidJournalpostStructureException.class,
				"DokumentInfo cannot contain dokumentvariant duplicates");
	}

	@Test
	public void shouldNotThrowExceptionWhenBothAvsenderMottakerFieldsAreSet() throws Exception {
		journalpost.setAvsenderMottaker(MOTTAKER);
		journalpost.setAvsenderMottakerId(AVSENDER_MOTTAKER_ID);

		validator.validate(journalpost);
	}

	@Test
	public void shouldNotThrowExceptionWhenAvsenderMottakerFieldsAreNull() throws Exception {
		journalpost.setAvsenderMottaker(null);
		journalpost.setAvsenderMottakerId(null);

		validator.validate(journalpost);
	}

	@Test
	public void shouldThrowExceptionAvsenderMottakerIsNull() throws Exception {
		journalpost.setAvsenderMottaker(null);
		journalpost.setAvsenderMottakerId(AVSENDER_MOTTAKER_ID);

		validateAndAssertExceptionThrownWithMessage(validator, ApplicationException.class,
				"Journalpost.AvsenderMottaker must be set when Journalpost.AvsenderMottakerId is set");
	}

	@Test
	public void shouldThrowExceptionAvsenderMottakerIdIsNull() throws Exception {
		journalpost.setAvsenderMottaker(MOTTAKER);
		journalpost.setAvsenderMottakerId(null);

		validateAndAssertExceptionThrownWithMessage(validator, ApplicationException.class,
				"Journalpost.AvsenderMottakerId must be set when Journalpost.AvsenderMottaker is set");
	}

	@Test
	public void shouldThrowExceptionAvsenderMottakerIdIsEmpty() throws Exception {
		journalpost.setAvsenderMottaker(MOTTAKER);
		journalpost.setAvsenderMottakerId("");

		validateAndAssertExceptionThrownWithMessage(validator, ApplicationException.class,
				"Journalpost.AvsenderMottakerId must be set when Journalpost.AvsenderMottaker is set");
	}

	@Test
	public void shouldAllowEmptyStringInAvsenderMottakerAndMapToNull() {
		/*Hack som bør tas vekk i HL4-2015. Konsument skal i HL3-2015 ha lov til å sende inn tom-streng på avsenderMottaker,
		 selv om feltet egentlig er påkrevd. Dette fordi konsument ikke har tilgang til AvsenderMottaker feltet før i
		 HL4. Fom. HL4-2015 bør det også valideres at AvsenderMottaker ikke er tom, dersom avsenderMotakkerId er satt.
		 Innført ifbm. PK-25537.
		*/

		journalpost.setAvsenderMottaker("");
		journalpost.setAvsenderMottakerId(AVSENDER_MOTTAKER_ID);

		validator.validate(journalpost);

		assertThat(journalpost.getAvsenderMottaker(), is(nullValue()));
	}


}
