package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark102;

import no.nav.service.dok.joark.nsb.exceptions.AlleredeFerdigstiltException;
import no.nav.service.dok.joark.nsb.exceptions.FeilStrukturException;
import no.nav.service.dok.joark.nsb.exceptions.KanIkkeFerdigstillesException;
import no.nav.service.dok.joark.nsb.exceptions.ObjektIkkeFunnetException;
import no.nav.service.dok.joark.nsb.exceptions.UgyldigInputException;
import no.nav.service.dok.joark.nsb.to.OppdaterJournalpostArkiverDokumentRequestTo;

/**
 * Interface for the operation OppdaterJournalpostArkiverDokument
 * 
 * @author Torgeir Cook.
 */
public interface OppdaterJournalpostArkiverDokumentService {

	/**
	 * Validates and updates the Journalpost identified by the request.
	 *
	 * @param requestTo
	 */
	void oppdaterJournalpostArkiverDokument(
			OppdaterJournalpostArkiverDokumentRequestTo requestTo) throws UgyldigInputException, ObjektIkkeFunnetException, KanIkkeFerdigstillesException, FeilStrukturException, AlleredeFerdigstiltException;
}
