package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark100;

/**
 * Interface for the operation OpprettJournalpostArkiverDokument
 *
 * @author Torgeir Cook.
 */
public interface OpprettJournalpostArkiverDokumentService {

	/**
	 * Validates, updates mandatory values and persists the Journalpost contained within the request
	 *
	 * @param request, The request containing the Journalpost to create.
	 * @return The response object containing the persisted journalpostId and dokumentId.
	 */
	OpprettJournalpostArkiverDokumentResponseTo opprettJournalpostArkiverDokument(
			OpprettJournalpostArkiverDokumentRequestTo request);
}
