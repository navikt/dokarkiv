package no.nav.dokarkiv.core.domain.codes;

import no.nav.dokarkiv.core.stelvio.CodesTablePeriodicItem;

/**
 * Enum for codes in T_K_REFERANSE_T.
 *
 * @author Per Kristian Foss, Visma Sirius
 */
public enum ReferanseTypeCode implements CtiRelationship<ReferanseTypeCode, String> {

	/**
	 * Spørsmål (brukerdialog)
	 */
	SPOERSMAAL;

	@Override
	public Class<? extends CodesTablePeriodicItem<ReferanseTypeCode, String>> getCtiClass() {
		return ReferanseTypeCti.class;
	}

}
