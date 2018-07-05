package no.nav.dokarkiv.core.domain.codes;

import no.nav.dokarkiv.core.stelvio.CodesTablePeriodicItem;

/**
 * Enum for codes in T_K_FAGSYSTEM.
 * 
 * @author Rune Romundstad, Sirius IT
 *
 */
public enum FagsystemCode implements CtiRelationship<FagsystemCode, String> {
	
	/**
	 * Arena
	 */
	AO01,
	/**
	 * Infotrygd
	 */
	IT01,
	/**
	 * Bidrag
	 */
	BID,	
	/**
	 * Pensjon
	 */
	PEN,
	/**
	 * Øvrig 
	 */
	OVR,
	/**
	 * Skanning 
	 */
	MOT,
	/**
	 * Okonomi 
	 */
	OKO,	
	/**
	 * Bidrag innkreving 
	 */
	BII,
	/**
	 * GOSYS
	 */
	FS22,
	/**
	 * GSAK
	 */
	FS19,
	/**
	 * Utbetalingsmeldinger (UR)
	 */
	OB36;

	/** {@inheritDoc} */
	@Override
	public Class<? extends CodesTablePeriodicItem<FagsystemCode, String>> getCtiClass() {
		return FagsystemCti.class;
	}
}
