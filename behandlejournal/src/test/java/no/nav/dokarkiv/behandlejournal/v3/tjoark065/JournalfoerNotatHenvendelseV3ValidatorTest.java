package no.nav.dokarkiv.behandlejournal.v3.tjoark065;

import static org.mockito.Mockito.verify;

import no.nav.dokarkiv.behandlejournal.v3.AbstractBehandleJournalJournalpostValidatorTest;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.exceptions.InvalidJournalpostStructureException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit tests of OpprettNotatJournalpostValidator.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
@RunWith(MockitoJUnitRunner.class)
public class JournalfoerNotatHenvendelseV3ValidatorTest extends AbstractBehandleJournalJournalpostValidatorTest {

	@InjectMocks
	private JournalfoerNotatHenvendelseV3Validator validator;

	@Test
	public void shouldDelegateToJournalpostStructureVerifierToVerifyJournalpostStructure() {
		validator.validate(journalpost);

		verify(journalpostStructureVerifierMock).verifyJournalpostStructure(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfNoBrukereOnJournalpost() {
		journalpost.clearBrukere();
		validateAndAssertExceptionThrownWithMessage(validator, ApplicationException.class, "Journalpost must have a bruker");
	}

	@Test
	public void shouldThrowExceptionIfFagomradeNotSetOnJournalpost() {
		journalpost.setFagomrade(null);
		validateAndAssertExceptionThrownWithMessage(validator, ApplicationException.class, "Fagomrade must be set");
	}

	@Test
	public void shouldThrowExceptionWhenDokInfoWithoutSensitivt() {
		journalpost = createJournalpostWithoutSensitivtDokInfo();
		validateAndAssertExceptionThrownWithMessage(validator, ApplicationException.class, "Sensitivt must be set");
	}

	@Test
	public void shouldThrowExceptionIfNoJournalfoerendeEnhetOnJournalpost() {
		journalpost.setJournalForendeEnhetId(null);
		validateAndAssertExceptionThrownWithMessage(validator, ApplicationException.class, "Field journalfoerendeEnhetId must be set");
	}

	@Test
	public void shouldThrowExceptionIfNoFildetaljerOnJournalpostOnJournalpost() {
		journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().clearFildetaljerListe();
		validateAndAssertExceptionThrownWithMessage(validator, ApplicationException.class, "FilDetaljer must be set");
	}

	@Test
	public void shouldThrowExceptionIfNoFileContentOnFildetaljerOnJournalpost() {
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
	public void shouldThrowExceptionIfNoOrganInterntOnJournalpost() {
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().setOrganInternt(null);
		validateAndAssertExceptionThrownWithMessage(validator, ApplicationException.class, "organInternt must be set");
	}

	@Test
	public void shouldThrowExceptionIfNoInnskrentPartsInnsynOnJournalpost() {
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().setInnskrenketPartsinnsyn(null);
		validateAndAssertExceptionThrownWithMessage(validator, ApplicationException.class, "innskrenketPartsinnsyn must be set");
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
