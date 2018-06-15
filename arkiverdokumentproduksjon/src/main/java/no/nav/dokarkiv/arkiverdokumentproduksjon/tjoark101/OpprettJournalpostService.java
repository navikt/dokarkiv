package no.nav.service.dok.joark.nsb;

import no.nav.service.dok.joark.nsb.to.OpprettJournalpostRequestTo;
import no.nav.service.dok.joark.nsb.to.OpprettJournalpostResponseTo;

/**
 * Interface for the operation OpprettJournalpostService
 * 
 * @author Stig Strøm
 */
public interface OpprettJournalpostService {
	
	/**
	 * Validates, updates mandatory values and persists the Journalpost contained within the request
	 * 
	 * @param opprettJournalpostRequest, The request containing the Journalpost to create.
	 * @return The response object containing the persisted journalpostId and dokumentId.
	 */
	OpprettJournalpostResponseTo opprettJournalpost(
			OpprettJournalpostRequestTo opprettJournalpostRequest);
}
