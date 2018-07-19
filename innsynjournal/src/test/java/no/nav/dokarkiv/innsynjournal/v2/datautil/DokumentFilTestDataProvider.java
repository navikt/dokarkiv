package no.nav.dokarkiv.innsynjournal.v2.datautil;

import no.nav.dokarkiv.core.domain.builder.DokumentFilBuilder;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;

/**
 * Provides helpers for building DokumentFil-instances
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
public final class DokumentFilTestDataProvider {

	private DokumentFilTestDataProvider() {
	}

	public static final String FIL_UUID = FilDetaljer.generateUuid();
	public static final byte[] FIL_CONTENT = "Test".getBytes();

	public static DokumentFilBuilder createDokumentFil() {
		return DokumentFilBuilder.getDokumentFilBuilder()
				.filUuid(FIL_UUID)
				.fil(FIL_CONTENT)
				.opprettetKildeNavn("test");
	}
}
