package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark100;

import no.nav.dokarkiv.core.domain.Journalpost;

/**
 * Interface for validating OpprettJournalpostArkiverDokumentService
 *
 * @author Stig Strøm
 */
public interface OpprettJournalpostArkiverDokumentValidator {

	/**
	 * Validates that all required fields are set and are valid.
	 *
	 * @param journalpost The Journalpost to validate.
	 * @param ferdigstillJournalpost whether the Journalpost should be ferdigstillt
	 */
	void validate(Journalpost journalpost, boolean ferdigstillJournalpost);
}
