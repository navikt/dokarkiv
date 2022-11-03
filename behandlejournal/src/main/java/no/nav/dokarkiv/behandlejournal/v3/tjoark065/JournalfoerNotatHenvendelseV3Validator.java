package no.nav.dokarkiv.behandlejournal.v3.tjoark065;

import no.nav.dokarkiv.behandlejournal.v3.AbstractBehandleJournalV3JournalpostValidator;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.journalbehandling.JournalpostStructureVerifier;
import no.nav.dokarkiv.core.journalbehandling.MandatoryFieldsVerifier;
import org.springframework.stereotype.Component;

@Component
public class JournalfoerNotatHenvendelseV3Validator extends AbstractBehandleJournalV3JournalpostValidator {

	public JournalfoerNotatHenvendelseV3Validator(MandatoryFieldsVerifier mandatoryFieldsVerifier, JournalpostStructureVerifier journalpostStructureVerifier) {
		super(mandatoryFieldsVerifier, journalpostStructureVerifier);
	}

	@Override
	public void validate(Journalpost journalpost) {
		performCommonValidation(journalpost);
		journalpostStructureVerifier.verifyJournalpostStructure(journalpost);
		validateBrukerIsSet(journalpost);
		validateCommonNotatFields(journalpost);
		validateCommonNotatDokumentInfo(journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next()
				.getDokumentInfo());
		validateFileContent(journalpost);
		validateFilDetaljer(journalpost);
		validateCustomDokumentInfoValues(journalpost);
	}
	
	private void validateCustomDokumentInfoValues(Journalpost journalpost) {
		DokumentInfo dokumentInfo = journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next()
				.getDokumentInfo();

		validateBrevkode(dokumentInfo);
	}
}
