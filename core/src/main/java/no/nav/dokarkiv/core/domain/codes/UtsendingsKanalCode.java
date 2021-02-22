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
	/** ALTINN */
	ALTINN,
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
	/** Trygderetten */
    TRYGDERETTEN,
	/**
	 * INGEN_DISTRIBUSJON
	 */
	INGEN_DISTRIBUSJON,
	/** midertidelig felt for migrering fra ondemand til dokarkiv, referanse sak: 5140 **/
	MIGRERING_S,
	/** midertidelig felt for migrering fra ondemand til dokarkiv, referanse sak: 5140 **/
	MIGRERING_L;

	/** {@inheritDoc} */
	@Override
	public Class<? extends CodesTablePeriodicItem<UtsendingsKanalCode, String>> getCtiClass() {
		return UtsendingsKanalCti.class;
	}

}
