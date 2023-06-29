package no.nav.dokarkiv.journalpost.v1.util;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import no.nav.dokarkiv.journalpost.v1.api.Sakstype;
import org.slf4j.MDC;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Metrikker for journalpostapi
 */
public final class JournalpostApiMetrics {

	public static final String DOK_JOURNALPOSTAPI_SAKSTYPE_COUNT = "dok_journalpostapi_sakstype_count";
	public static final String DOK_JOURNALPOSTAPI_EKSTERNREFERANSEID_IKKESATT_COUNT = "dok_journalpostapi_eksternreferanseid_ikkesatt_count";
	public static final String DOK_JOURNALPOSTAPI_OPPDATERING_AV_AVSENDER_MED_DIGITAL_MOTTAKSKANAL_COUNT = "dok_journalpostapi_oppdatering_av_avsender_med_digital_mottakskanal_count";
	public static final String TAG_TJENESTE = "tjeneste";
	public static final String TAG_SAKSTYPE = "sakstype";
	public static final String TAG_CONSUMERID = "consumerid";

	private JournalpostApiMetrics() {
		// noop
	}

	public static void incrementSakstypeCounter(Sakstype sakstype, String tjeneste, MeterRegistry meterRegistry) {
		String consumerId = MDC.get(MDC_CONSUMER_ID);

		if (sakstype != null && consumerId != null) {
			Counter.builder(DOK_JOURNALPOSTAPI_SAKSTYPE_COUNT)
					.tags(TAG_TJENESTE, tjeneste)
					.tags(TAG_SAKSTYPE, sakstype.name())
					.tags(TAG_CONSUMERID, consumerId)
					.register(meterRegistry)
					.increment();
		} else if (sakstype == null && consumerId != null) {
			Counter.builder(DOK_JOURNALPOSTAPI_SAKSTYPE_COUNT)
					.tags(TAG_TJENESTE, tjeneste)
					.tags(TAG_SAKSTYPE, Sakstype.ARKIVSAK.name() + "_sakstype_null")
					.tags(TAG_CONSUMERID, consumerId)
					.register(meterRegistry)
					.increment();
		}
	}

	// Teller for å se hvilke klienter som ikke setter eksternReferanseId
	// Analyse for å se om vi kan sette feltet som påkrevd
	public static void incrementEksternReferanseIdIkkeSattCounter(String eksternReferanseId, MeterRegistry meterRegistry) {
		if (isBlank(eksternReferanseId)) {
			String consumerId = MDC.get(MDC_CONSUMER_ID);

			Counter.builder(DOK_JOURNALPOSTAPI_EKSTERNREFERANSEID_IKKESATT_COUNT)
					.tags(TAG_CONSUMERID, consumerId)
					.register(meterRegistry)
					.increment();
		}
	}

	// Teller for å se hvilke klienter som oppdaterer avsender når mottakskanal på journalpost er digital
	// Analyse for å se om vi kan sette feltet som påkrevd
	public static void incrementOppdateringAvAvsenderMedDigitalMottakskanalCounter(MeterRegistry meterRegistry) {
		String consumerId = MDC.get(MDC_CONSUMER_ID);

		Counter.builder(DOK_JOURNALPOSTAPI_OPPDATERING_AV_AVSENDER_MED_DIGITAL_MOTTAKSKANAL_COUNT)
				.tags(TAG_CONSUMERID, consumerId)
				.register(meterRegistry)
				.increment();
	}
}