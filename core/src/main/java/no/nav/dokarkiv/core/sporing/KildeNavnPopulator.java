package no.nav.dokarkiv.core.sporing;

import no.nav.dokarkiv.core.domain.entities.Journalpost;

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
