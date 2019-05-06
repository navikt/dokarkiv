package no.nav.dokarkiv.core.datautil;

import static no.nav.dokarkiv.core.domain.builder.SaksrelasjonBuilder.getSaksrelasjonBuilder;

import no.nav.dokarkiv.core.domain.builder.SaksrelasjonBuilder;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;

/**
 * Provides helpers for building SaksRelasjon-instances
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
public final class SaksrelasjonTestDataProvider {

	public static final String SAK_KILDE_NAVN = "test";
	public static final FagsystemCode SAK_FAGSYSTEM = FagsystemCode.PEN;
	public static final String SAK_ID = "9999";
	public static final String PEN_SAK_ID = "1000";

	private SaksrelasjonTestDataProvider() {

	}

	public static SaksrelasjonBuilder createSaksrelasjon() {
		return SaksrelasjonBuilder
				.getSaksrelasjonBuilder()
				.sakId(SAK_ID)
				.fagsystem(SAK_FAGSYSTEM)
				.feilregistrert(false)
				.opprettetKildeNavn(SAK_KILDE_NAVN);
	}
	
	public static SaksrelasjonBuilder createSaksrelasjon(boolean feilregistrert) {
		return SaksrelasjonBuilder
				.getSaksrelasjonBuilder()
				.sakId(SAK_ID)
				.fagsystem(SAK_FAGSYSTEM)
				.feilregistrert(feilregistrert)
				.opprettetKildeNavn(SAK_KILDE_NAVN);
	}

	public static Saksrelasjon createPENSaksrelasjon() {
		return getSaksrelasjonBuilder()
				.sakId(PEN_SAK_ID)
				.fagsystem(FagsystemCode.PEN)
				.opprettetKildeNavn("itest")
				.build();
	}
}
