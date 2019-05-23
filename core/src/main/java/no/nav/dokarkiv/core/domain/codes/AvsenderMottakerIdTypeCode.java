package no.nav.dokarkiv.core.domain.codes;

import no.nav.dokarkiv.core.stelvio.CodesTablePeriodicItem;

/**
 * @author Olav Røstvold Thorsen, Visma Consulting.
 */
public enum AvsenderMottakerIdTypeCode implements CtiRelationship<AvsenderMottakerIdTypeCode, String> {

	FNR,
	ORGNR,
	HPRNR,
	UTL_ORG;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<? extends CodesTablePeriodicItem<AvsenderMottakerIdTypeCode, String>> getCtiClass() {
		return AvsenderMottakerIdTypeCti.class;
	}
}
