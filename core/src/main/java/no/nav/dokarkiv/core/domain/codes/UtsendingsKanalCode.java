package no.nav.dokarkiv.core.domain.codes;

import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.stelvio.CodesTablePeriodicItem;

import static java.lang.String.format;

/**
 * Enum for codes in T_K_UTSENDINGS_KANAL.
 *
 * @author Per Kristian Foss, Visma Sirius
 */
public enum UtsendingsKanalCode implements CtiRelationship<UtsendingsKanalCode, String> {

	/**
	 * EESSI
	 */
	EESSI,
	/**
	 * ALTINN
	 */
	ALTINN,
	/**
	 * Ditt NAV
	 */
	NAV_NO,
	/**
	 * Sentral print
	 */
	S,
	/**
	 * Lokal print
	 */
	L,
	/**
	 * Sikker digital post
	 */
	SDP,
	/**
	 * EIA
	 */
	EIA,
	/**
	 * Helsenettet
	 */
	HELSENETTET,
	/**
	 * Trygderetten
	 */
	TRYGDERETTEN,
	/**
	 * INGEN_DISTRIBUSJON
	 */
	INGEN_DISTRIBUSJON,
	/**
	 * Midertidelig felt for migrering fra ondemand til dokarkiv, referanse sak: 5140
	 **/
	MIGRERING_S,
	/**
	 * Midlertidig felt for migrering fra ondemand til dokarkiv, referanse sak: 5140
	 **/
	MIGRERING_L,
	/**
	 * Innlogget samtale
	 */
	NAV_NO_CHAT,
	/**
	 * Taushetsbelagt digital post til virksomhet
	 */
	DPVT;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<? extends CodesTablePeriodicItem<UtsendingsKanalCode, String>> getCtiClass() {
		return UtsendingsKanalCti.class;
	}

	public static UtsendingsKanalCode fromString(String utsendingskanal) {
		try {
			return UtsendingsKanalCode.valueOf(utsendingskanal.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new InputValideringFeiletException(format("%s er ikke en gyldig utsendingskanal", utsendingskanal));
		}
	}

}
