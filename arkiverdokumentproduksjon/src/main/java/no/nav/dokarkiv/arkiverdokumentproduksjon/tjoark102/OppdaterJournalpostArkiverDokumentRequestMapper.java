package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark102;

import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigInputException;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OppdaterJournalpostArkiverDokumentRequest;

/**
 * Mapper for ArkiverDokumentOgFerdigstillJournalpostRequest from arkiverdokumentproduksjon-tjenestespesifikasjon
 * to domain
 *
 * @author Torgeir Cook
 */
public interface OppdaterJournalpostArkiverDokumentRequestMapper {
	/**
	 * Map from ws request to domain request.
	 *
	 * @param wsRequest The ws request
	 * @return The domain request
	 */
	OppdaterJournalpostArkiverDokumentRequestTo map(OppdaterJournalpostArkiverDokumentRequest wsRequest) throws UgyldigInputException;
}
