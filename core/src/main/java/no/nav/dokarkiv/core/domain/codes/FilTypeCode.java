package no.nav.dokarkiv.core.domain.codes;

import no.nav.dokarkiv.core.stelvio.CodesTablePeriodicItem;

/**
 * Enum for codes in T_K_FIL_T.
 *
 * @author Thomas Eugen Bjørge, Sirius IT
 */
public enum FilTypeCode implements CtiRelationship<FilTypeCode, String> {

	PDF,
	PDFA,
	XML,
	RTF,
	AFP,
	META,
	DLF,
	JPEG,
	TIFF,
	DOC,
	DOCX,
	XLS,
	XLSX,
	AXML,
	DXML,
	JSON;
	
	/** {@inheritDoc} */
	@Override
	public Class<? extends CodesTablePeriodicItem<FilTypeCode, String>> getCtiClass() {
		return FilTypeCti.class;
	}
	
}
