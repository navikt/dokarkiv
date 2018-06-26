package no.nav.dokarkiv.core.journabehandling;


import no.nav.dokarkiv.core.domain.entities.Journalpost;
/**
 * Verifies the journalpost structure, i.e. the object graph satrting with the
 * Journalpost.
 *
 * @author Thomas Eugen Bj�rge, Visma Sirius
 */
public interface JournalpostStructureVerifier {

	/**
	 * Verifies the Journalpost structure.
	 *
	 * @param journalpost The Journalpost to verify,
	 */
	void verifyJournalpostStructure(Journalpost journalpost);

}
