package no.nav.dokarkiv.core.domain.codes;

import no.nav.dokarkiv.core.stelvio.CodesTablePeriodicItem;

/**
 * Enum for codes in T_K_FAGSYSTEM.
 * 
 * @author Rune Romundstad, Sirius IT
 *
 */
public enum FagsystemCode implements CtiRelationship<FagsystemCode, String> {
	
	/**
	 * Pensjon
	 */
	PEN,
	/**
	 * GOSYS
	 */
	FS22;

	/** {@inheritDoc} */
	@Override
	public Class<? extends CodesTablePeriodicItem<FagsystemCode, String>> getCtiClass() {
		return FagsystemCti.class;
	}
}
