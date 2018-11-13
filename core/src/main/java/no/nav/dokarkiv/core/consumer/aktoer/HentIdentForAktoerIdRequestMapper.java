package no.nav.dokarkiv.core.consumer.aktoer;

import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.HentIdentForAktoerIdRequest;
import org.springframework.stereotype.Component;

/**
 * Mapper for HentIdentForAktoerIdRequestTo
 *
 * @author Ketill Fenne, Visma Consulting.
 */
@Component
public class HentIdentForAktoerIdRequestMapper {

	/**
	 * Maps from domain object {@link HentIdentForAktoerIdRequestTo} to ws-object {@link HentIdentForAktoerIdRequest}
	 *
	 * @param requestTo The domain object to map
	 * @return The mapped ws-object
	 */
	public HentIdentForAktoerIdRequest map(HentIdentForAktoerIdRequestTo requestTo) {
		HentIdentForAktoerIdRequest wsRequest = new HentIdentForAktoerIdRequest();
		wsRequest.setAktoerId(requestTo.getAktoerId());
		return wsRequest;
	}
}
