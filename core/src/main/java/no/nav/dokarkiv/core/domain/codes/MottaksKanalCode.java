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
	/** PSELV */
	PSELV,
	/** Skanning Pensjon */
	SKAN_PEN,
	/** Skanning Nets */
	SKAN_NETS,
	/** E-post */
	E_POST,
	/** NETS - postboks 1400 */
	NETS_PB1400,
	/** NETS - postboks 1405 */
	NETS_PB1405,
	/** NETS - postboks 1406 */
	NETS_PB1406,
	/** NETS - postboks 1407 */
	NETS_PB1407,
	/** NETS - postboks 1408 */
	NETS_PB1408,
	/** NETS - postboks 1411 */
	NETS_PB1411,	
	/** NETS - postboks 1412 */
	NETS_PB1412,
	/** NETS - postboks 1413 */
	NETS_PB1413,
	/** NETS - postboks 1423 */
	NETS_PB1423,
	/** NETS - postboks 1431 */
	NETS_PB1431,
	/** NETS - postboks 1441 */
	NETS_PB1441,
	/** Eksternt oppslag */
	EKST_OPPS;

	/** {@inheritDoc} */
	@Override
	public Class<? extends CodesTablePeriodicItem<MottaksKanalCode, String>> getCtiClass() {
		return MottaksKanalCti.class;
	}	

}
