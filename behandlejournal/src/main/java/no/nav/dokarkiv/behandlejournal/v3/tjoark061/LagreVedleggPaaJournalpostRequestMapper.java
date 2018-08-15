package no.nav.dokarkiv.behandlejournal.v3.tjoark061;

/**
 * Mapper for LagreVedleggPaaJournalpostRequest from FIM to domain request.
 *  
 * @author Rune Romundstad, Visma Consulting
 */
public interface LagreVedleggPaaJournalpostRequestMapper {

	/**
	 * Map from LagreVedleggPaaJournalpost WS request to domain request
	 * @param wsRequest The WS request to map
	 * @return the LagreVedleggPaaJournalpostRequest domain object.
	 */
	LagreVedleggPaaJournalpostRequest map(
			no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.LagreVedleggPaaJournalpostRequest wsRequest);
}
