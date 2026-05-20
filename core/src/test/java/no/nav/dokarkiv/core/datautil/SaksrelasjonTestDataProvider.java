package no.nav.dokarkiv.core.datautil;

import no.nav.dokarkiv.core.domain.builder.SaksrelasjonBuilder;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;

import static no.nav.dokarkiv.core.domain.builder.SaksrelasjonBuilder.getSaksrelasjonBuilder;

/**
 * Provides helpers for building SaksRelasjon-instances
 */
public final class SaksrelasjonTestDataProvider {

	public static final String SAK_KILDE_NAVN = "test";
	public static final FagsystemCode SAK_FAGSYSTEM = FagsystemCode.FS22;
	public static final Long SAK_ID = 9999L;
	public static final Long PEN_SAK_ID = 1000L;

	private SaksrelasjonTestDataProvider() {

	}

	public static SaksrelasjonBuilder createSaksrelasjon() {
		return SaksrelasjonBuilder
				.getSaksrelasjonBuilder()
				.sakId(SAK_ID)
				.fagsystem(SAK_FAGSYSTEM)
				.feilregistrert(false);
	}

	public static SaksrelasjonBuilder createSaksrelasjon(boolean feilregistrert) {
		return SaksrelasjonBuilder
				.getSaksrelasjonBuilder()
				.sakId(SAK_ID)
				.fagsystem(SAK_FAGSYSTEM)
				.feilregistrert(feilregistrert);
	}
	public static SaksrelasjonBuilder createSaksrelasjonWithSak(Long sakId) {
		return SaksrelasjonBuilder
				.getSaksrelasjonBuilder()
				.sakId(sakId)
				.fagsystem(SAK_FAGSYSTEM)
				.feilregistrert(false);
	}

	public static Saksrelasjon createPENSaksrelasjonWithSak(Long sakId) {
		return getSaksrelasjonBuilder()
				.sakId(sakId)
				.fagsystem(FagsystemCode.PEN)
				.build();
	}
}
