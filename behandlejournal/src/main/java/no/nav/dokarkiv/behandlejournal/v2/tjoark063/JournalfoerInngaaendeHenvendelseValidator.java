package no.nav.dokarkiv.behandlejournal.v2.tjoark063;

import no.nav.dokarkiv.behandlejournal.v2.AbstractBehandleJournalJournalpostValidator;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.journalbehandling.JournalpostStructureVerifier;
import no.nav.dokarkiv.core.journalbehandling.MandatoryFieldsVerifier;
import org.springframework.stereotype.Component;

@Component
public class JournalfoerInngaaendeHenvendelseValidator extends AbstractBehandleJournalJournalpostValidator {

	public JournalfoerInngaaendeHenvendelseValidator(MandatoryFieldsVerifier mandatoryFieldsVerifier, JournalpostStructureVerifier journalpostStructureVerifier) {
		super(mandatoryFieldsVerifier, journalpostStructureVerifier);
	}

	@Override
	public void validate(Journalpost journalpost) {
		performCommonValidation(journalpost);
		journalpostStructureVerifier.verifyJournalpostStructure(journalpost);
		validateMandatoryInngaendeFields(journalpost);
		validateBrukerIsSet(journalpost);
		validateCustomJournalpostValues(journalpost);
		validateCustomDokumentInfoValues(journalpost);
		validateFileContent(journalpost);
		validateFilDetaljer(journalpost);
	}

	private void validateCustomJournalpostValues(Journalpost journalpost) {
		validateAvsenderMottaker(journalpost);
	}

	private void validateCustomDokumentInfoValues(Journalpost journalpost) {
		DokumentInfo dokumentInfo = journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next()
				.getDokumentInfo();

		validateBrevkode(dokumentInfo);
		validateSensitivt(dokumentInfo);
		validateTittel(dokumentInfo);
		validateKategori(dokumentInfo);
	}
}
