package no.nav.dokarkiv.core.domain.codes;

import no.nav.dokarkiv.core.stelvio.CodesTablePeriodicItem;

/**
 * @author Olav Røstvold Thorsen, Visma Consulting.
 */
public enum AvsenderMottakerTypeCode implements CtiRelationship<AvsenderMottakerTypeCode, String> {

	FNR,
	ORGNR,
	HPRNR,
	UTL_ORG;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<? extends CodesTablePeriodicItem<AvsenderMottakerTypeCode, String>> getCtiClass() {
		return AvsenderMottakerTypeCti.class;
	}
}
