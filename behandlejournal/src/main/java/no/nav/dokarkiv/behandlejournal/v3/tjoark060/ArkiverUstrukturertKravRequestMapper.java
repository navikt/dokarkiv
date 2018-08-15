package no.nav.dokarkiv.behandlejournal.v3.tjoark060;

/**
 * Mapper for ArkiverUstrukturertKravRequest from FIM (MOD) to domain request.
 * 
 * @author Rune Romundstad, Visma Consulting
 *
 */
public interface ArkiverUstrukturertKravRequestMapper {

	/**
	 * Map from ArkiverUstrukturertKravRequest WS request to domain request.
	 * @param wsRequest the ArkiverUstrukturertKravRequest WS object
	 * @return the domain ArkiverUstrukturertKravRequest object.
	 */
	ArkiverUstrukturertKravRequest map(
			no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.ArkiverUstrukturertKravRequest wsRequest);
}
