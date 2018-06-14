package no.nav.dokarkiv.core.domain.codes;

import no.nav.dokarkiv.core.stelvio.CodesTablePeriodicItem;

/**
 * Enum for codes in T_K_VARIANT_FORMAT.
 *
 * @author Per Kristian Foss, Visma Sirius
 */
public enum VariantFormatCode implements CtiRelationship<VariantFormatCode, String> {
	
	/**
	 * Produksjonsformat
	 */
	PRODUKSJON,
	/**
	 * Arkivformat
	 */
	ARKIV,
	/**
	 * SkanningMetadata
	 */
	SKANNING_META,
	/**
	 * BrevbestillingData
	 */
	BREVBESTILLING,
	/**
	 * Originalformat
	 */
	ORIGINAL,
	/**
	 * Sladdetformat
	 */
	SLADDET,
	/**
	 * Produksjonsformat DLF
	 */
	PRODUKSJON_DLF,
	/**
	 * versjon med infotekster
	 */
	FULLVERSJON;

	/** {@inheritDoc} */
	@Override
	public Class<? extends CodesTablePeriodicItem<VariantFormatCode, String>> getCtiClass() {
		return VariantFormatCti.class;
	}
}
