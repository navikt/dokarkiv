package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark103;

import no.nav.service.dok.joark.NoJournalpostFoundException;
import no.nav.service.dok.joark.journalbehandling.UgyldigJournalStatusOvergangException;
import no.nav.service.dok.joark.nsb.to.AvbrytJournalpostRequestTo;


/**
 * Interface for the operation AvbrytJournalpostService
 * 
 * @author Stig Strøm
 */
public interface AvbrytJournalpostService {
	
	/**
	 * Sets the Journalpost in Interrupted state.
	 * 
	 * @param domainRequest with journalpostId and endreAvNavn 
	 * @throws NoJournalpostFoundException when journalpost cannot be found
	 * @throws UgyldigJournalStatusOvergangException when journalpost is in a not interruptable state
	 */
	void avbrytJournalpost(AvbrytJournalpostRequestTo domainRequest) throws NoJournalpostFoundException,
			UgyldigJournalStatusOvergangException;
}
