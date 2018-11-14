package no.nav.dokarkiv.core.domain.codes;

import no.nav.dokarkiv.core.stelvio.CodesTablePeriodicItem;

/**
 * Enum for codes in T_K_BEGRENSNING_TYPE.
 * 
 * @author Ketill Fenne, Visma Consulting
 */
public enum BegrensningTypeCode implements CtiRelationship<BegrensningTypeCode, String> {
	/**
	 * Utilgjengeliggjort
	 */
	UTILGJENGELIGGJORT,
	/**
	 * Skjermet
	 */
	SKJERMET;

	/** {@inheritDoc} */
	@Override
	public Class<? extends CodesTablePeriodicItem<BegrensningTypeCode, String>> getCtiClass() {
		return BegrensningTypeCti.class;
	}

}
