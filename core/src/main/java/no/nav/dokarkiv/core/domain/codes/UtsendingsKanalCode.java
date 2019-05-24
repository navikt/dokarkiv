package no.nav.dokarkiv.core.domain.codes;

import no.nav.dokarkiv.core.stelvio.CodesTablePeriodicItem;

/**
 * Enum for codes in T_K_UTSENDINGS_KANAL. 
 * 
 * @author Per Kristian Foss, Visma Sirius
 */
public enum UtsendingsKanalCode implements CtiRelationship<UtsendingsKanalCode, String> {

	/** EESSI */
	EESSI,
	/** Ditt NAV */
	NAV_NO,
	/** Sentral print */
	S,
	/** Lokal print */
	L,
	/** Sikker digital post */
	SDP,
	/** EIA */
	EIA,
	/** Helsenettet */
	HELSENETTET,
	/** eFormidling */
	EFORMIDLING,
	/**
	 * INGEN_DISTRIBUSJON
	 */
	INGEN_DISTRIBUSJON;

	/** {@inheritDoc} */
	@Override
	public Class<? extends CodesTablePeriodicItem<UtsendingsKanalCode, String>> getCtiClass() {
		return UtsendingsKanalCti.class;
	}

}
