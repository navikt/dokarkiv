package no.nav.dokarkiv.core.journalbehandling;

import no.nav.dokarkiv.core.domain.Journalpost;

/**
 * Verifies the journalpost structure, i.e. the object graph satrting with the
 * Journalpost.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public interface JournalpostStructureVerifier {

	/**
	 * Verifies the Journalpost structure.
	 *
	 * @param journalpost The Journalpost to verify,
	 */
	void verifyJournalpostStructure(Journalpost journalpost);

}
