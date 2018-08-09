package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark103;

import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigJournalStatusOvergangException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;


/**
 * Interface for the operation AvbrytJournalpostService
 *
 * @author Stig Str�m
 */
public interface AvbrytJournalpostService {

	/**
	 * Sets the Journalpost in Interrupted state.
	 *
	 * @param domainRequest with journalpostId and endreAvNavn
	 * @throws NoJournalpostFoundException           when journalpost cannot be found
	 * @throws UgyldigJournalStatusOvergangException when journalpost is in a not interruptable state
	 */
	void avbrytJournalpost(AvbrytJournalpostRequestTo domainRequest) throws NoJournalpostFoundException,
			UgyldigJournalStatusOvergangException;
}
