package no.nav.dokarkiv.core.domain.codes;

import no.nav.dokarkiv.core.stelvio.CodesTablePeriodicItem;

/**
 * Enum for codes in T_K_FAKT_DIS_KANAL.
 * 
 * @author Thomas Eugen Bjørge, Sirius IT
 */
public enum FaktiskDistribusjonskanalCode implements CtiRelationship<FaktiskDistribusjonskanalCode, String>{
	/**
	 * Elektronisk distribusjon
	 */
	E,
	/**
	 * Sentral print
	 */
	S,
	/**
	 * Lokal print
	 */
	L;

	/** {@inheritDoc} */
	@Override
	public Class<? extends CodesTablePeriodicItem<FaktiskDistribusjonskanalCode, String>> getCtiClass() {
		return FaktiskDistribusjonskanalCti.class;
	}

}
