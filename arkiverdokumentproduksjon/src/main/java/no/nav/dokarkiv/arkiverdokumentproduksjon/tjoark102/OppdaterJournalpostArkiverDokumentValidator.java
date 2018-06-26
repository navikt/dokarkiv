package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark102;

import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.AlleredeFerdigstiltException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.FeilStrukturException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.KanIkkeFerdigstillesException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.ObjektIkkeFunnetException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigInputException;
import no.nav.dokarkiv.core.domain.entities.Journalpost;

/**
 * Interface for validating OppdaterJournalpostArkiverDokumentValidator
 *
 * @author Torgeir Cook
 */
public interface OppdaterJournalpostArkiverDokumentValidator {

	/**
	 * Validates fields in input params before journalpost and dokumentinfo are updated.
	 *
	 * @param journalpost The Journalpost to be updated
	 * @param request     The requestTo
	 */
	void validate(Journalpost journalpost, OppdaterJournalpostArkiverDokumentRequestTo request) throws ObjektIkkeFunnetException, FeilStrukturException, KanIkkeFerdigstillesException, AlleredeFerdigstiltException, UgyldigInputException;

	void validateRequest(OppdaterJournalpostArkiverDokumentRequestTo request) throws ObjektIkkeFunnetException, UgyldigInputException;
}
