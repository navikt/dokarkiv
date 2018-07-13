package no.nav.dokarkiv.behandlejournal.v2.tjoark061;

import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.LagreVedleggPaaJournalpostResponse;

/**
 * Mapper for LagreVedleggPaaJournalpostResponse from domain to FIM response.
 * 
 * @author Rune Romundstad, Visma Consulting
 */
public interface LagreVedleggPaaJournalpostResponseMapper {

	/**
	 * Map from LagreVedleggPaaJournal domain response to WS response.
	 * 
	 * @param domainResponse The domain response to map.
	 * @return the LagreVedleggPaaJournalResponse WS object.
	 */
	LagreVedleggPaaJournalpostResponse map(
			no.nav.dokarkiv.behandlejournal.v2.tjoark061.LagreVedleggPaaJournalpostResponse domainResponse);
}
