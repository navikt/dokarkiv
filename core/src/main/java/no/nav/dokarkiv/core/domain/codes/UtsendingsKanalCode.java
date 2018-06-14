package no.nav.dokarkiv.core.domain.codes;

import no.nav.dokarkiv.core.stelvio.CodesTablePeriodicItem;

/**
 * Enum for codes in T_K_UTSENDINGS_KANAL. 
 * 
 * @author Per Kristian Foss, Visma Sirius
 */
public enum UtsendingsKanalCode implements CtiRelationship<UtsendingsKanalCode, String> {

	/** PSELV */
	PSELV,
	/** EESSI */
	EESSI,
	/** ALTINN */
	ALTINN,
	/** Ditt NAV */
	NAV_NO,
	/** E-post */
	E_POST,
	/** Sentral print */
	S,
	/** Lokal print */
	L,
	/** Sikker digital post */
	SDP,
	/** EIA */
	EIA;

	/** {@inheritDoc} */
	@Override
	public Class<? extends CodesTablePeriodicItem<UtsendingsKanalCode, String>> getCtiClass() {
		return UtsendingsKanalCti.class;
	}

}
