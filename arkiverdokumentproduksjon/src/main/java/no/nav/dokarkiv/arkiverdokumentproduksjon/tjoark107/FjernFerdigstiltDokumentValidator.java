package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark107;

import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.NoDokumentInfoFoundException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigDokumentStatusVerdiException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigJournalStatusVerdiException;
import no.nav.dokarkiv.core.domain.entities.Journalpost;

/**
 * Interface for FjernFerdigstiltDokumentValidator. Used for validating request and that the journalpost is in correct state.
 *
 * @author Stig Strøm
 */
public interface FjernFerdigstiltDokumentValidator {

	/**
	 * Validates if the journalpost and dokumentInfoId can be set back to Under Redigering status.
	 *
	 * @param journalpost the journalpost itself
	 * @param request     the input request
	 * @throws UgyldigJournalStatusVerdiException  Thrown
	 * @throws NoDokumentInfoFoundException        Thrown if the dokumentInfo in the request cannot be found in the journalpost
	 * @throws UgyldigDokumentStatusVerdiException Thrown if documentInfo is AVBRUTT or UNDER_REDIGERING
	 * @throws NoJournalpostFoundException         Thrown if no journalpost is found
	 */
	void validate(Journalpost journalpost, FjernFerdigstiltDokumentRequestTo request)
			throws UgyldigJournalStatusVerdiException, NoDokumentInfoFoundException, UgyldigDokumentStatusVerdiException,
			NoJournalpostFoundException;

	/**
	 * Validates input request.
	 *
	 * @param request
	 */
	void validateInputRequest(FjernFerdigstiltDokumentRequestTo request);

}