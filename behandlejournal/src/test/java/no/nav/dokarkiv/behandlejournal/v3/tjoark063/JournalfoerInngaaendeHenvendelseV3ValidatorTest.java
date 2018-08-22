package no.nav.dokarkiv.behandlejournal.v3.tjoark063;

import no.nav.dokarkiv.behandlejournal.v3.AbstractBehandleJournalV3JournalpostValidatorTest;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.exceptions.InvalidJournalpostStructureException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit tests for JournalfoerInngaaendeHenvendelseValidator.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
@RunWith(MockitoJUnitRunner.class)
public class JournalfoerInngaaendeHenvendelseV3ValidatorTest extends
		AbstractBehandleJournalV3JournalpostValidatorTest {

	@InjectMocks
	private JournalfoerInngaaendeHenvendelseV3Validator validator;

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
	public void shouldThrowExceptionIfDokumentDatoNotSetOnJournalpost() {
		journalpost.setDokumentDato(null);
		validateAndAssertExceptionThrownWithMessage(validator, ApplicationException.class, "DokumentDato must be set");
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
	public void shouldThrowExceptionIfNoJournalfoerendeEnhetOnJournalpost() {
		journalpost.setJournalForendeEnhetId(null);
		validateAndAssertExceptionThrownWithMessage(validator, ApplicationException.class, "Field journalfoerendeEnhetId must be set");
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
	public void shouldThrowExceptionIfNoSaksrelasjonOnJournalpost() {
		journalpost.setSaksrelasjon(null);
		validateAndAssertExceptionThrownWithMessage(validator, ApplicationException.class,
				"Missing parameter on journalpost: saksrelasjon");
	}

	@Test
	public void shouldThrowExceptionIfNoBrukereOnJournalpost() {
		journalpost.clearBrukere();
		validateAndAssertExceptionThrownWithMessage(validator, ApplicationException.class,
				"Journalpost must have a bruker");
	}

	@Test
	public void shouldThrowExceptionIfNoBrevkodeOnDokumentInfo() {
		DokumentInfo dokumentInfo = journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next()
				.getDokumentInfo();
		dokumentInfo.setBrevkode(null);
		validateAndAssertExceptionThrownWithMessage(validator, ApplicationException.class, "Brevkode must be set");
	}

	@Test
	public void shouldThrowExceptionIfNoInnskrenketPartsinnsynOnDokumentInfo() {
		DokumentInfo dokumentInfo = journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next()
				.getDokumentInfo();
		dokumentInfo.setInnskrenketPartsinnsyn(null);
		validateAndAssertExceptionThrownWithMessage(validator, ApplicationException.class,
				"InnskrenketPartsinnsyn must be set");
	}

	@Test
	public void shouldThrowExceptionIfNoSensitivtOnDokumentInfo() {
		DokumentInfo dokumentInfo = journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next()
				.getDokumentInfo();
		dokumentInfo.setSensitivt(null);
		validateAndAssertExceptionThrownWithMessage(validator, ApplicationException.class, "Sensitivt must be set");
	}

	@Test
	public void shouldThrowExceptionIfNoTittel() {
		DokumentInfo dokumentInfo = journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next()
				.getDokumentInfo();
		dokumentInfo.setTittel(null);
		validateAndAssertExceptionThrownWithMessage(validator, ApplicationException.class, "Tittel must be set");
	}

	@Test
	public void shouldThrowExceptionIfNoKategori() {
		DokumentInfo dokumentInfo = journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next()
				.getDokumentInfo();
		dokumentInfo.setKategori(null);
		validateAndAssertExceptionThrownWithMessage(validator, ApplicationException.class, "Kategori must be set");
	}

	@Test
	public void shouldThrowExceptionIfNoDokumentInfoObjectOnJournalpost() {
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().setDokumentInfo(null);
		validateAndAssertExceptionThrownWithMessage(validator, ApplicationException.class,
				"Journalpost must have a DokumentInfo");
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
