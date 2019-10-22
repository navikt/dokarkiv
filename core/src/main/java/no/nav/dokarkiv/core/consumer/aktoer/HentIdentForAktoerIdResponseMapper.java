package no.nav.dokarkiv.core.consumer.aktoer;

import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.HentIdentForAktoerIdResponse;
import org.springframework.stereotype.Component;

@Component
public class HentIdentForAktoerIdResponseMapper {

	public HentIdentForAktoerIdResponseTo map(HentIdentForAktoerIdResponse response) {
		return new HentIdentForAktoerIdResponseTo(response.getIdent(), response.getHistoriskeIdenter());
	}
}
