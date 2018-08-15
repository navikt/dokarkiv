package no.nav.dokarkiv.behandlejournal.v3.tjoark065;

import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.JournalfoerNotatResponse;

/**
 * Mapper for JournalfoerNotatHenvendelseResponse from domain(JOARK) to FIM (MOD)
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public interface JournalfoerNotatHenvendelseResponseMapper {
	/**
	 * Map from domain response to ws response.
	 *
	 * @param domainResponse The domain response
	 * @return The ws response
	 */
	JournalfoerNotatResponse map(
			JournalfoerNotatHenvendelseResponse domainResponse);
}
