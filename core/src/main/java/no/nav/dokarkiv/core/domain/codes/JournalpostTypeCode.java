package no.nav.dokarkiv.core.domain.codes;

import no.nav.dokarkiv.core.stelvio.CodesTablePeriodicItem;

/**
 * Enum for codes in T_K_JOURNALPOST_T.
 *
 * @author Per Kristian Foss, Visma Sirius
 */
public enum JournalpostTypeCode implements CtiRelationship<JournalpostTypeCode, String> {

	/**
	 * Inngående dokument
	 */
	I,
	/**
	 * Utgående dokument
	 */
	U,
	/**
	 * Internt notat
	 */
	N;

	/** {@inheritDoc} */
	@Override
	public Class<? extends CodesTablePeriodicItem<JournalpostTypeCode, String>> getCtiClass() {
		return JournalpostTypeCti.class;
	}
}
