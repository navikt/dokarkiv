package no.nav.dokarkiv.core.consumer.aktoer;

import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.HentIdentForAktoerIdRequest;
import org.springframework.stereotype.Component;

@Component
public class HentIdentForAktoerIdRequestMapper {

	public HentIdentForAktoerIdRequest map(HentIdentForAktoerIdRequestTo requestTo) {
		HentIdentForAktoerIdRequest wsRequest = new HentIdentForAktoerIdRequest();
		wsRequest.setAktoerId(requestTo.getAktoerId());
		return wsRequest;
	}

}
