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
	/** Skanning Iron Mountain */
	SKAN_IM,
	/** Eksternt oppslag */
	EKST_OPPS,
	/** Helsenettet */
	HELSENETTET,
	/**Ditt NAV uten ID-porten-pålogging**/
	NAV_NO_UINNLOGGET,
	/** Innsendt av NAV-ansatt **/
	INNSENDT_NAV_ANSATT,
	/** Innlogget chat **/
	NAV_NO_CHAT	;

	/** {@inheritDoc} */
	@Override
	public Class<? extends CodesTablePeriodicItem<MottaksKanalCode, String>> getCtiClass() {
		return MottaksKanalCti.class;
	}	

}
