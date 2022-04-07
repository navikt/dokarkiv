package no.nav.dokarkiv.journalpost.v1.util;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import no.nav.dokarkiv.journalpost.v1.api.Sakstype;
import org.slf4j.MDC;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;

/**
 * Metrikker for journalpostapi
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
public final class JournalpostApiMetrics {
	private JournalpostApiMetrics() {
		// noop
	}

	public static void incrementSakstypeCounter(Sakstype sakstype, String tjeneste, MeterRegistry meterRegistry) {
		String consumerId = MDC.get(MDC_CONSUMER_ID);
		if(sakstype != null && consumerId != null) {
			Counter.builder("dok_journalpostapi_sakstype_count")
					.tags("tjeneste", tjeneste)
					.tags("sakstype", sakstype.name())
					.tags("consumerid", consumerId)
					.register(meterRegistry)
					.increment();
		}
	}
}
