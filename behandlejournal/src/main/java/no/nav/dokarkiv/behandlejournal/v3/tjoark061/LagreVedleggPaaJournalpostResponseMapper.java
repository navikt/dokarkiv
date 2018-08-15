package no.nav.dokarkiv.behandlejournal.v3.tjoark061;

import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.LagreVedleggPaaJournalpostResponse;

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
			no.nav.dokarkiv.behandlejournal.v3.tjoark061.LagreVedleggPaaJournalpostResponse domainResponse);
}
