package no.nav.dokarkiv.behandlejournal.v2.tjoark065;

import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.JournalfoerNotatRequest;

/**
 * Mapper for JournalfoerNotatHenvendelseRequest from FIM (MOD) to domain (JOARK)
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public interface JournalfoerNotatHenvendelseRequestMapper {
	/**
	 * Map from ws request to domain request.
	 *
	 * @param wsRequest The ws request
	 * @return The domain request
	 */
	JournalfoerNotatHenvendelseRequest map(
			JournalfoerNotatRequest wsRequest);
}
