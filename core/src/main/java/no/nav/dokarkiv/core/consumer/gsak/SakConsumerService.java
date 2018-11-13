package no.nav.dokarkiv.core.consumer.gsak;

import no.nav.dokarkiv.core.consumer.gsak.domain.SakInfoTo;
import no.nav.dokarkiv.core.consumer.gsak.hentgsaksaker.GsakConsumer;
import org.springframework.stereotype.Component;

@Component
public class SakConsumerService {
	private GsakConsumer gsakConsumer;

	public SakConsumerService(GsakConsumer gsakConsumer) {
		this.gsakConsumer = gsakConsumer;
	}

	public String hentAktoerForSak(String sakId) {
		SakInfoTo gsakSakTo = gsakConsumer.hentSakInfo(sakId);
		return gsakSakTo.getAktoerId();
	}
}
