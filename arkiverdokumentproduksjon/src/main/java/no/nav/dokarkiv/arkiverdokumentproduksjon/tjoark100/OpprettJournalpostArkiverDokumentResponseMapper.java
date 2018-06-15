package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark100;

import no.nav.service.dok.joark.nsb.to.OpprettJournalpostArkiverDokumentResponseTo;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostArkiverDokumentResponse;

/**
 * Mapper for OpprettJournalpostArkiverDokumentResponse from domain response to ws response.
 *
 * @author Torgeir Cook
 */
public interface OpprettJournalpostArkiverDokumentResponseMapper {
	/**
	 * Map from domain response to ws response.
	 *
	 * @param domainResponse
	 * @return wsResponse
	 */
	OpprettJournalpostArkiverDokumentResponse map(
			OpprettJournalpostArkiverDokumentResponseTo domainResponse);
}
