package no.nav.dokarkiv.innsynjournal.v2.datautil;

import no.nav.dokarkiv.core.domain.builder.SkannetInnholdBuilder;

/**
 * Provides helpers for building SkannetInnhold-instances
 *
 * @author Torgeir Cook, Visma Consulting.
 */
public final class SkannetInnholdTestDataProvider {

	public static final String DOKUMENT_TYPE_ID = "dokumentTypeId";
	public static final String VEDLEGG_INNHOLD = "vedlegg_innhold";

	private SkannetInnholdTestDataProvider() {

	}

	public static SkannetInnholdBuilder createSkannetInnhold() {
		return SkannetInnholdBuilder
				.getSkannetInnholdBuilder()
				.opprettetKildeNavn(DOKUMENT_TYPE_ID)
				.dokumenttypeId(DOKUMENT_TYPE_ID)
				.vedleggInnhold(VEDLEGG_INNHOLD);
	}
}
