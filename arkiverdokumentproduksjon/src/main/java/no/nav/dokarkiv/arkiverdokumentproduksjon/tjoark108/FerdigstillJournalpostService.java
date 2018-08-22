package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark108;

import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigDokumentStatusVerdiException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigJournalStatusVerdiException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;

/**
 * Interface for the operation FerdigstillJournalpostService
 * 
 * @author Stig Strøm
 */
public interface FerdigstillJournalpostService {

	/**
	 * Sets a journalpost to Ferdigstilt
	 * 
	 * @param domainRequest
	 *            the domain request
	 * @throws NoJournalpostFoundException
	 *             Thrown when cannot find the journalpost in the request
	 * @throws UgyldigJournalStatusVerdiException
	 *             Thrown when the journalpost is not under production(not JournalStatusCode.D)
	 * @throws UgyldigDokumentStatusVerdiException
	 *             Thrown if the journalpost have documents UNDER_REDIGERING
	 */
	void ferdigstillJournalpost(FerdigstillJournalpostRequestTo domainRequest) throws NoJournalpostFoundException,
			UgyldigJournalStatusVerdiException, UgyldigDokumentStatusVerdiException;
}
