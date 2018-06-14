package no.nav.dokarkiv.core.domain.codes;

import no.nav.dokarkiv.core.stelvio.CodesTablePeriodicItem;

/**
 * Enum for codes in T_K_ARSAK_RETUR.
 *
 * @author Per Kristian Foss, Visma Sirius
 */
public enum ArsakReturCode implements CtiRelationship<ArsakReturCode, String> {

	/**
	 * Flyttet - adresse ukjent
	 */
	FLYTTET_ADR_UKJ,
	/**
	 * Flyttet - ettersendingstiden utløpt
	 */
	FLYTTET_TID_UTL,
	/**
	 * Ikke hentet
	 */
	IKKE_HENTET,
	/**
	 * Utilstrekkelig adresse
	 */
	UTILSTREKKELIG_ADR,
	/**
	 * annet
	 */
	ANNET;

	/** {@inheritDoc} */
	@Override
	public Class<? extends CodesTablePeriodicItem<ArsakReturCode, String>> getCtiClass() {
		return ArsakReturCti.class;
	}
}
