package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark112;

import no.nav.dokarkiv.core.domain.entities.Journalpost;

/**
 * Interface for validating OpprettJournalpostArkiverDokumenterService
 *
 * @author Stig Strøm
 */
public interface OpprettJournalpostArkiverDokumenterValidator {

	/**
	 * Validates that all required fields are set and are valid.
	 *
	 * @param journalpost The Journalpost to validate.
	 */
	void validate(Journalpost journalpost);
}
