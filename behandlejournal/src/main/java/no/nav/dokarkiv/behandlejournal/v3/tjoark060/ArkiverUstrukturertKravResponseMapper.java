package no.nav.dokarkiv.behandlejournal.v3.tjoark060;

import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.ArkiverUstrukturertKravResponse;

/**
 * Mapper for ArkiverUstrukturertKravResponse from domain response to FIM (MOD) reponse.
 * 
 * @author Rune Romundstad, Visma Consulting
 *
 */
public interface ArkiverUstrukturertKravResponseMapper {

	/**
	 * Map from domain response object to WS reponse object.
	 * @param domainResponse the domain ArkiverUstrukturertKravResponse object.
	 * @return the WS ArkiverUstrukturertKravResponse object.
	 */
	ArkiverUstrukturertKravResponse map(no.nav.dokarkiv.behandlejournal.v3.tjoark060.ArkiverUstrukturertKravResponse domainResponse);
}
