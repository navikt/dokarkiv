package no.nav.dokarkiv.behandlejournal.v3.tjoark063;

import no.nav.dokarkiv.behandlejournal.v3.AbstractBehandleJournalJournalpostValidator;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.springframework.stereotype.Component;

/**
 * Validates Journalpost for journalfoerInngaaendeHenvendelse
 * operation.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
@Component
public class JournalfoerInngaaendeHenvendelseV3Validator extends
		AbstractBehandleJournalJournalpostValidator {

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
		validateInnskrenketPartsinnsyn(dokumentInfo);
		validateSensitivt(dokumentInfo);
		validateTittel(dokumentInfo);
		validateKategori(dokumentInfo);
	}
}
