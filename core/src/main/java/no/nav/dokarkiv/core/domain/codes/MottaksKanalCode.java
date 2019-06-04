package no.nav.dokarkiv.core.domain.codes;

import no.nav.dokarkiv.core.stelvio.CodesTablePeriodicItem;

/**
 * Enum for codes in T_K_MOTTAKS_KANAL. 
 * 
 * @author Per Kristian Foss, Visma Sirius
 */
public enum MottaksKanalCode implements CtiRelationship<MottaksKanalCode, String> {

	/** EESSI */
	EESSI,
	/** EIA */
	EIA,
	/** nav.no */
	NAV_NO,
	/** ALTINN */
	ALTINN,
	/** Skanning Pensjon */
	SKAN_PEN,
	/** Skanning Nets */
	SKAN_NETS,
	/** Eksternt oppslag */
	EKST_OPPS,
	/** Helsenettet */
	HELSENETTET;

	/** {@inheritDoc} */
	@Override
	public Class<? extends CodesTablePeriodicItem<MottaksKanalCode, String>> getCtiClass() {
		return MottaksKanalCti.class;
	}	

}
