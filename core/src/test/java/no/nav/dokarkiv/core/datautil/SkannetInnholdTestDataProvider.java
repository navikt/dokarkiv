package no.nav.dokarkiv.core.datautil;

import no.nav.dokarkiv.core.domain.builder.SkannetInnholdBuilder;

/**
 * Provides helpers for building SkannetInnhold-instances
 */
public final class SkannetInnholdTestDataProvider {

	public static final String DOKUMENT_TYPE_ID = "dokumentTypeId";
	public static final String VEDLEGG_INNHOLD = "vedlegg_innhold";

	private SkannetInnholdTestDataProvider() {

	}

	public static SkannetInnholdBuilder createSkannetInnhold() {
		return SkannetInnholdBuilder
				.getSkannetInnholdBuilder()
				.dokumenttypeId(DOKUMENT_TYPE_ID)
				.vedleggInnhold(VEDLEGG_INNHOLD);
	}
}
