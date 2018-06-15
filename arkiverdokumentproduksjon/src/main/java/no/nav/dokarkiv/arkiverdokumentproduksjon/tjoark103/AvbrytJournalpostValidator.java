package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark103;

import no.nav.domain.dok.joark.Journalpost;
import no.nav.service.dok.joark.journalbehandling.UgyldigJournalStatusOvergangException;

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
	 * @throws ApplicationException if Journalpost cannot be interrupted
	 */
	void validate(Journalpost journalpost) throws UgyldigJournalStatusOvergangException;

}
