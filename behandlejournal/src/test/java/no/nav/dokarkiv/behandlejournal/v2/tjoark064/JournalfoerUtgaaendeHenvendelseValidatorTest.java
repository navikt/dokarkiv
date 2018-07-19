package no.nav.dokarkiv.behandlejournal.v2.tjoark064;

import no.nav.dokarkiv.behandlejournal.v2.AbstractBehandleJournalJournalpostValidatorTest;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.exceptions.InvalidJournalpostStructureException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit tests of JournalfoerUtgaaendeHenvendelseValidator.
 * 
 * @author Joakim Bjørnstad, Visma Consulting
 */
@RunWith(MockitoJUnitRunner.class)
public class JournalfoerUtgaaendeHenvendelseValidatorTest extends
		AbstractBehandleJournalJournalpostValidatorTest {

	@InjectMocks
	private JournalfoerUtgaaendeHenvendelseValidator validator;

	@Test
	public void shouldThrowExceptionIfNoBrukereOnJournalpost() {
		journalpost.clearBrukere();
		validateAndAssertExceptionThrownWithMessage(validator, ApplicationException.class,
				"Journalpost must have a bruker");
	}

	@Test
	public void shouldThrowExceptionIfFagomradeNotSetOnJournalpost() {
		journalpost.setFagomrade(null);
		validateAndAssertExceptionThrownWithMessage(validator, ApplicationException.class, "Fagomrade must be set");
	}

	@Test
	public void shouldThrowExceptionIfUtsendingsKanalNotSetOnJournalpost() {
		journalpost.setUtsendingskanal(null);
		validateAndAssertExceptionThrownWithMessage(validator, ApplicationException.class,
				"Utsendingskanal must be set");
	}
	
	@Test
	public void shouldThrowExceptionIfJournalforendeEnhetIdNotSetOnJournalpost() {
		journalpost.setJournalForendeEnhetId(null);
		validateAndAssertExceptionThrownWithMessage(validator, ApplicationException.class,
				"Field journalfoerendeEnhetId must be set");
	}
	
	@Test
	public void shouldThrowExceptionIfOpprettetAvNavnNotSetOnJournalpost() {
		journalpost.setOpprettetAvNavn(null);
		validateAndAssertExceptionThrownWithMessage(validator, ApplicationException.class, "OpprettetAvNavn must be set");
	}
	
	@Test
	public void shouldThrowExceptionIfInnholdNotSetOnJournalpost() {
		journalpost.setInnhold(null);
		validateAndAssertExceptionThrownWithMessage(validator, ApplicationException.class, "Innhold(Tittel) must be set");
	}

	@Test
	public void shouldThrowExceptionIfAvsenderMottakerNotSetOnJournalpost() {
		journalpost.setAvsenderMottaker(null);
		validateAndAssertExceptionThrownWithMessage(validator, ApplicationException.class,
				"AvsenderMottaker must be set");
	}

	@Test
	public void shouldThrowExceptionIfAvsenderMottakerIdNotSetOnJournalpost() {
		journalpost.setAvsenderMottakerId(null);
		validateAndAssertExceptionThrownWithMessage(validator, ApplicationException.class,
				"AvsenderMottakerId must be set");
	}

	@Test
	public void shouldThrowExceptionIfNoFildetaljerOnJournalpostOnJournalpost() {
		journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().clearFildetaljerListe();
		validateAndAssertExceptionThrownWithMessage(validator, ApplicationException.class, "FilDetaljer must be set");
	}

	@Test
	public void shouldThrowExcpetionIfNoFileContentOnFildetaljerOnJournalpost() {
		journalpost.findAllFilDetaljer().get(0).setFileContent(null);
		validateAndAssertExceptionThrownWithMessage(validator, ApplicationException.class,
				"FilDetaljer must have filecontent");
	}

	@Test
	public void shouldThrowExceptionIfDokumentDuplicatesOnJournalpost() {
		addDuplicatesOfVariantFormats(journalpost);
		validateAndAssertExceptionThrownWithMessage(validator, InvalidJournalpostStructureException.class,
				"DokumentInfo cannot contain dokumentvariant duplicates");
	}

	@Test
	public void shouldThrowExceptionIfNoDokumentInfoObjectOnJournalpost() {
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().setDokumentInfo(null);
		validateAndAssertExceptionThrownWithMessage(validator, ApplicationException.class,
				"Journalpost must have a DokumentInfo");
	}

	@Test
	public void shouldThrowExceptionIfNoSaksrelasjonOnJournalpost() {
		journalpost.setSaksrelasjon(null);
		validateAndAssertExceptionThrownWithMessage(validator, ApplicationException.class,
				"Missing parameter on journalpost: saksrelasjon");
	}

	@Test
	public void shouldThrowExceptionIfNoSensitivtOnJournalpost() {
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().setSensitivt(null);
		validateAndAssertExceptionThrownWithMessage(validator, ApplicationException.class, "Sensitivt must be set");
	}

	@Test
	public void shouldThrowExceptionIfNoInnskrenketPartsinnsynOnJournalpost() {
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo()
				.setInnskrenketPartsinnsyn(null);
		validateAndAssertExceptionThrownWithMessage(validator, ApplicationException.class,
				"InnskrenketPartsinnsyn must be set");
	}

	@Test
	public void shouldThrowExceptionIfNoBrevkodeOnJournalpost() {
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().setBrevkode(null);
		validateAndAssertExceptionThrownWithMessage(validator, ApplicationException.class, "Brevkode must be set");
	}
	
	@Test
	public void shouldThrowExceptionIfVariantFormatIsMissing() {
		journalpost.findAllFilDetaljer().get(0).setVariantFormat(null);
		validateAndAssertExceptionThrownWithMessage(validator, ApplicationException.class, "FilDetaljer must have variantformat");
	}
	
	@Test
	public void shouldThrowExceptionifFilTypeIsMissing() {
		journalpost.findAllFilDetaljer().get(0).setFiltype(null);
		validateAndAssertExceptionThrownWithMessage(validator, ApplicationException.class, "FilDetaljer must have filtype");
	}

}
