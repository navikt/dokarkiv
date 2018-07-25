package no.nav.dokarkiv.core.datautil;

import static no.nav.dokarkiv.core.domain.builder.BrukerBuilder.getBrukerBuilder;

import no.nav.dokarkiv.core.domain.builder.BrukerBuilder;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;

/**
 * Provides helpers for building Bruker-instances
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
public final class BrukerTestDataProvider {


	public static final String BRUKER_ID = "***gammelt_fnr***";

	private BrukerTestDataProvider() {

	}

	public static BrukerBuilder createBruker() {
		return getBrukerBuilder()
				.brukerId(BRUKER_ID)
				.brukerType(BrukerTypeCode.PERSON)
				.opprettetKildeNavn("test");
	}

	public static Bruker createBruker(String id, BrukerTypeCode brukerTypeCode) {
		return getBrukerBuilder()
				.brukerId(id)
				.brukerType(brukerTypeCode)
				.opprettetKildeNavn("itest")
				.build();
	}
}
