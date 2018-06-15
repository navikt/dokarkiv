package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark102;

import no.nav.domain.dok.joark.Journalpost;
import no.nav.service.dok.joark.nsb.exceptions.AlleredeFerdigstiltException;
import no.nav.service.dok.joark.nsb.exceptions.FeilStrukturException;
import no.nav.service.dok.joark.nsb.exceptions.KanIkkeFerdigstillesException;
import no.nav.service.dok.joark.nsb.exceptions.ObjektIkkeFunnetException;
import no.nav.service.dok.joark.nsb.exceptions.UgyldigInputException;
import no.nav.service.dok.joark.nsb.to.OppdaterJournalpostArkiverDokumentRequestTo;

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
	 * @param request The requestTo
	 */
	void validate(Journalpost journalpost, OppdaterJournalpostArkiverDokumentRequestTo request) throws ObjektIkkeFunnetException, FeilStrukturException, KanIkkeFerdigstillesException, AlleredeFerdigstiltException, UgyldigInputException;

	void validateRequest(OppdaterJournalpostArkiverDokumentRequestTo request) throws ObjektIkkeFunnetException, UgyldigInputException;
}
