package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark102;


import no.nav.dokarkiv.core.exceptions.AlleredeFerdigstiltException;
import no.nav.dokarkiv.core.exceptions.FeilStrukturException;
import no.nav.dokarkiv.core.exceptions.KanIkkeFerdigstillesException;
import no.nav.dokarkiv.core.exceptions.ObjektIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.UgyldigInputException;

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
