package no.nav.dokarkiv.core.consumer.aktoer;

import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.HentAktoerIdForIdentRequest;
import org.springframework.stereotype.Component;

/**
 * Mapper for HentAktoerIdForIdentRequestTo
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
@Component
public class HentAktoerIdForIdentRequestMapper {

	/**
	 * Maps from domain object {@link HentAktoerIdForIdentRequestTo} to ws-object {@link HentAktoerIdForIdentRequest}
	 *
	 * @param requestTo The domain object to map
	 * @return The mapped ws-object
	 */
	public HentAktoerIdForIdentRequest map(HentAktoerIdForIdentRequestTo requestTo) {
		HentAktoerIdForIdentRequest wsRequest = new HentAktoerIdForIdentRequest();
		wsRequest.setIdent(requestTo.getIdent());
		return wsRequest;
	}
}
