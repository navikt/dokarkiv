package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark112;

import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostArkiverDokumenterRequest;

/**
 * Interface for the operation OpprettJournalpostArkiverDokument
 *
 * @author Torgeir Cook.
 */
public interface OpprettJournalpostArkiverDokumenterService {

	/**
	 * Validates, updates mandatory values and persists the Journalpost contained within the request
	 *
	 * @param request, The request containing the Journalpost to create.
	 * @return The response object containing the persisted journalpostId and dokumentId.
	 */
	OpprettJournalpostArkiverDokumenterResponseTo opprettJournalpostArkiverDokument(
			OpprettJournalpostArkiverDokumenterRequest request);
}
