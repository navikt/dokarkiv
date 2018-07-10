package no.nav.dokarkiv.behandlejournal.v2.tjoark062;

import no.nav.dokarkiv.behandlejournal.v2.exceptions.NoJournalpostFoundException;

/**
 * Defines the contract for the FerdigstillDokumentOpplasting operation.
 * 
 * @author Joakim Bjørnstad, Visma Consulting
 * 
 */
public interface FerdigstillDokumentopplasting {

	/**
	 * Operation that finishes the dokumentopplasting. Updates journalstatus on
	 * the journalpost.
	 * 
	 * @param ferdigstillDokumentOpplastingRequest
	 *            The request object. Contains the journalpostId
	 * @throws NoJournalpostFoundException
	 */
	void ferdigstillDokumentOpplasting(FerdigstillDokumentopplastingRequest ferdigstillDokumentOpplastingRequest)
			throws NoJournalpostFoundException;

}
