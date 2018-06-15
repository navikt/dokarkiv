package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark105;

import no.nav.service.dok.joark.NoJournalpostFoundException;
import no.nav.service.dok.joark.nsb.to.ArkiverVedleggRequestTo;
import no.nav.service.dok.joark.nsb.to.ArkiverVedleggResponseTo;

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
