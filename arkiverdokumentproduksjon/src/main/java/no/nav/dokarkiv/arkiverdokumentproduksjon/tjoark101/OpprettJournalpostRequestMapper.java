package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101;

import no.nav.service.dok.joark.nsb.to.OpprettJournalpostRequestTo;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostRequest;


/**
 * Mapper for OpprettJournalpostRequestMapper from arkiverdokumentproduksjon-tjenestespesifikasjon
 * to domain (JOARK)
 *
 * @author Stig Strøm 
 */
public interface OpprettJournalpostRequestMapper {
	/**
	 * Map from ws request to domain request.
	 *
	 * @param wsRequest The ws request
	 * @return The domain request
	 */
	OpprettJournalpostRequestTo map(OpprettJournalpostRequest wsRequest);
}
