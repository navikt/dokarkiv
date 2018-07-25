package no.nav.dokarkiv.core.datautil;

import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;

import no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;

/**
 * Provides helpers for building FilDetaljer-instances
 *
 * @author Roar Bjurstrom, Visma Consulting.
 * @author Thomas Kåsene, Visma Consulting AS
 */
public final class FildetaljerTestDataProvider {

	public static final FilTypeCode FIL_TYPE = FilTypeCode.PDF;
	public static final VariantFormatCode VARIANT_FORMAT = VariantFormatCode.ARKIV;

	private FildetaljerTestDataProvider() {
	}

	public static FilDetaljerBuilder createFildetaljerFil(String filuid) {
		return getFilDetaljerBuilder()
				.opprettetKildeNavn("test")
				.filtype(FIL_TYPE)
				.filUuid(filuid)
				.variantFormat(VARIANT_FORMAT);
	}

	public static FilDetaljerBuilder createFildetaljer(String filuid, String onDemandId) {
		return createFildetaljerFil(filuid)
				.onDemandId(onDemandId);
	}

	public static FilDetaljerBuilder createFilDetaljerArkiv() {
		return getFilDetaljerBuilder()
				.fileContent("file".getBytes())
				.filUuid(FilDetaljer.generateUuid())
				.filtype(FIL_TYPE)
				.variantFormat(VARIANT_FORMAT)
				.opprettetKildeNavn("test");
	}


	public static FilDetaljerBuilder createFilDetaljerProduksjon() {
		return getFilDetaljerBuilder()
				.fileContent("file".getBytes())
				.filUuid(FilDetaljer.generateUuid())
				.filtype(FIL_TYPE)
				.variantFormat(VariantFormatCode.PRODUKSJON)
				.opprettetKildeNavn("test");
	}

	public static FilDetaljer createFilDetaljerArkivPDFA() {
		return getFilDetaljerBuilder()
				.filtype(FilTypeCode.PDFA)
				.variantFormat(VariantFormatCode.ARKIV)
				.opprettetKildeNavn("itest")
				.build();
	}

	public static FilDetaljer createFilDetaljerProduksjonXML() {
		return getFilDetaljerBuilder()
				.filtype(FilTypeCode.XML)
				.variantFormat(VariantFormatCode.PRODUKSJON)
				.opprettetKildeNavn("itest")
				.build();
	}
}
