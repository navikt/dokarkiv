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

	public static final String DOK_JOURNALPOSTAPI_SAKSTYPE_COUNT = "dok_journalpostapi_sakstype_count";
	public static final String TAG_TJENESTE = "tjeneste";
	public static final String TAG_SAKSTYPE = "sakstype";
	public static final String TAG_CONSUMERID = "consumerid";

	private JournalpostApiMetrics() {
		// noop
	}

	public static void incrementSakstypeCounter(Sakstype sakstype, String tjeneste, MeterRegistry meterRegistry) {
		String consumerId = MDC.get(MDC_CONSUMER_ID);
		if(sakstype != null && consumerId != null) {
			Counter.builder(DOK_JOURNALPOSTAPI_SAKSTYPE_COUNT)
					.tags(TAG_TJENESTE, tjeneste)
					.tags(TAG_SAKSTYPE, sakstype.name())
					.tags(TAG_CONSUMERID, consumerId)
					.register(meterRegistry)
					.increment();
		} else if(sakstype == null && consumerId != null) {
			Counter.builder(DOK_JOURNALPOSTAPI_SAKSTYPE_COUNT)
					.tags(TAG_TJENESTE, tjeneste)
					.tags(TAG_SAKSTYPE, Sakstype.ARKIVSAK.name())
					.tags(TAG_CONSUMERID, consumerId)
					.register(meterRegistry)
					.increment();
		} else {
			//noop
		}
	}
}
