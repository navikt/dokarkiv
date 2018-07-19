package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark103;

import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigJournalStatusOvergangException;
import no.nav.dokarkiv.core.domain.entities.Journalpost;

/**
 * Interface for validating AvbrytJournalpost
 *
 * @author Stig Strøm
 */
public interface AvbrytJournalpostValidator {

	/**
	 * Checks if the Journalpost can be interrupted
	 *
	 * @param journalpost The Journalpost to be updated
	 * @throws UgyldigJournalStatusOvergangException if Journalpost cannot be interrupted
	 */
	void validate(Journalpost journalpost) throws UgyldigJournalStatusOvergangException;

}
