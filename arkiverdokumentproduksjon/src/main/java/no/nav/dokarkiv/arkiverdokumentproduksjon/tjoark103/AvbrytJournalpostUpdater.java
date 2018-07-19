package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark103;

import no.nav.dokarkiv.core.domain.entities.Journalpost;

/**
 * Interface for AvbrytJournalpostUpdater.
 *
 * @author Stig Strøm
 */
public interface AvbrytJournalpostUpdater {

	/**
	 * Set the journalpost to Interrupted State
	 *
	 * @param journalpost journalpost to update
	 * @param endretAvNavn one who updates the journalpost
	 * @return the updated journalpost
	 */
	Journalpost updateJournalpost(Journalpost journalpost, String endretAvNavn);
}
