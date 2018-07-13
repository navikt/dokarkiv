package no.nav.dokarkiv.behandlejournal.v2;

import no.nav.dokarkiv.core.domain.entities.Journalpost;

/**
 * Journalpost validator used by behandleJournal operations.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
public interface BehandleJournalJournalpostValidator {

	/**
	 * Validates that all required fields are set and are valid.
	 *
	 * @param journalpost The Journalpost to validate.
	 */
	void validate(Journalpost journalpost);
}
