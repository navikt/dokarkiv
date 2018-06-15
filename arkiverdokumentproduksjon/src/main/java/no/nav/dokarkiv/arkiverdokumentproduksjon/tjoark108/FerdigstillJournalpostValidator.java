package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark108;

import no.nav.domain.dok.joark.Journalpost;
import no.nav.service.dok.joark.journalbehandling.UgyldigDokumentStatusVerdiException;
import no.nav.service.dok.joark.journalbehandling.UgyldigJournalStatusVerdiException;
import no.nav.service.dok.joark.nsb.to.FerdigstillJournalpostRequestTo;

/**
 * Interface for validting FerdigstillJournalpost
 * 
 * @author Stig Strøm
 *
 */
public interface FerdigstillJournalpostValidator {
	
	/**
	 * Checks if the mandatory fields in the input request is set
	 *  
	 * @param request the input request
	 */
	void validateInputRequest(FerdigstillJournalpostRequestTo request);
	
	/**
	 * Validates the structure and status of the journalpost
	 * 
	 * @param journalpost the journalpost
	 * @throws UgyldigJournalStatusVerdiException The journalpost is not under production
	 * @throws UgyldigDokumentStatusVerdiException The journalpost have documents UNDER_REDIGERING
	 */
	void validate(Journalpost journalpost) throws UgyldigJournalStatusVerdiException, UgyldigDokumentStatusVerdiException;
}
