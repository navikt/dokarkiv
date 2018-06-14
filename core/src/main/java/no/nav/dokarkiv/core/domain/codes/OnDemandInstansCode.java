package no.nav.dokarkiv.core.domain.codes;

import no.nav.dokarkiv.core.stelvio.CodesTablePeriodicItem;

/**
 * Codes for OnDemandInstansCti
 * @author Hans Olav Loftum, BEKK
 */
public enum OnDemandInstansCode implements CtiRelationship<OnDemandInstansCode, String> {
	
	PESYS,
	INFOT_UT,
	SYFO;

	/** {@inheritDoc} */
	@Override
	public Class<? extends CodesTablePeriodicItem<OnDemandInstansCode, String>> getCtiClass() {
		return OnDemandInstansCti.class;
	}
}
