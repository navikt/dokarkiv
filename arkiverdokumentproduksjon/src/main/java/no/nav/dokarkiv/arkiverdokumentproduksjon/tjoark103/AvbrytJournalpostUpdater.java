package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark103;

import no.nav.domain.dok.joark.Journalpost;

/**
 * Interface for AvbrytJournalpostUpdater. 
 * 
 * @author Stig Strøm
 *
 */
public interface AvbrytJournalpostUpdater {
	
	/**
	 * Set the journalpost to Interrupted State
	 * 
	 * @param the journalpost to update
	 * @param the one who updates the journalpost
	 * @return the updated journalpost
	 */
	Journalpost updateJournalpost(Journalpost journalpost, String endretAvNavn);
}
