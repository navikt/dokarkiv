package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark100;

import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostArkiverDokumentRequest;

/**
 * Mapper for OpprettJournalpostArkiverDokumentRequestMapper from arkiverdokumentproduksjon-tjenestespesifikasjon
 * to domain
 *
 * @author Stig Strøm
 */
public interface OpprettJournalpostArkiverDokumentRequestMapper {
	/**
	 * Map from ws request to domain request.
	 *
	 * @param wsRequest The ws request
	 * @return The domain request
	 */
	OpprettJournalpostArkiverDokumentRequestTo map(OpprettJournalpostArkiverDokumentRequest wsRequest);
}
