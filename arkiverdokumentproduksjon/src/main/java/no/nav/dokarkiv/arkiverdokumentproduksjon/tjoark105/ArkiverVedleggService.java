package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark105;

import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;

/**
 * Interface for the operation ArkiverVedlegg
 *
 * @author Magnar Brandsdal, Visma Consulting
 */
public interface ArkiverVedleggService {

	/**
	 * Validate and updates a journalpost with the dokumentInfo included in the request.
	 *
	 * @param arkiverVedleggRequest Request containing journalpostId and document to be persisted
	 * @return The response with journalpostId and dokumentInfoId
	 * @throws NoJournalpostFoundException
	 */
	ArkiverVedleggResponseTo arkiverVedlegg(ArkiverVedleggRequestTo arkiverVedleggRequest) throws NoJournalpostFoundException;

}
