package no.nav.dokarkiv.core.domain.codes;

import no.nav.dokarkiv.core.stelvio.CodesTablePeriodicItem;

/**
 * Enum for codes in K_BRUKER_T.
 *
 * @author Per Kristian Foss, Visma Sirius
 */
public enum BrukerTypeCode implements CtiRelationship<BrukerTypeCode, String> {

	/**
	 * PERSON
	 */
	PERSON,
	/**
	 * ORGANISASJON
	 */
	ORGANISASJON,
	/**
	 * SAMHANDLER
	 */
	SAMHANDLER;

	/** {@inheritDoc} */
	@Override
	public Class<? extends CodesTablePeriodicItem<BrukerTypeCode, String>> getCtiClass() {
		return BrukerTypeCti.class;
	}
}
