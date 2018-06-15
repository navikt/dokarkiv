package no.nav.dokarkiv.core.journabehandling;


import no.nav.dokarkiv.core.domain.Journalpost;

/**
 * Populates opprettetKildeNavn/endretKildeNavn for the entire Journalpost object graph.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public interface KildeNavnPopulator {

	/**
	 * Populate opprettetKildeNavn/endretKildeNavn.
	 *
	 * @param journalpost The Journalpost to update.
	 * @param kildeNavn The kildeNavn to set.
	 */
	void populateKildeNavnForEntireJournalStructure(Journalpost journalpost, String kildeNavn);

}
