package no.nav.dokarkiv.core.datautil;

import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;

import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;

/**
 * Provides helpers for building FilDetaljer-instances
 */
public final class FildetaljerTestDataProvider {

	private FildetaljerTestDataProvider() {
	}

	public static FilDetaljer createFilDetaljerArkivPDFA() {
		return getFilDetaljerBuilder()
				.filtype(FilTypeCode.PDFA)
				.variantFormat(VariantFormatCode.ARKIV)
				.build();
	}

	public static FilDetaljer createFilDetaljerProduksjonXML() {
		return getFilDetaljerBuilder()
				.filtype(FilTypeCode.XML)
				.variantFormat(VariantFormatCode.PRODUKSJON)
				.build();
	}

	public static FilDetaljer createFilDetaljerSladdetPDFA() {
		return getFilDetaljerBuilder()
				.filtype(FilTypeCode.PDFA)
				.variantFormat(VariantFormatCode.SLADDET)
				.build();
	}
}
