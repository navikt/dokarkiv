package no.nav.dokarkiv.behandlejournal.v3.tjoark065;

import no.nav.dokarkiv.behandlejournal.v3.AbstractBehandleJournalJournalpostValidator;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.springframework.stereotype.Component;

/**
 * Validates Journalpost for journalfoerNotatHenvendelse operations.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
@Component
public class JournalfoerNotatHenvendelseV3Validator extends AbstractBehandleJournalJournalpostValidator {

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
