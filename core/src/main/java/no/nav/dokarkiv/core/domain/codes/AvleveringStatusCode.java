package no.nav.dokarkiv.core.domain.codes;

/**
 * Enum for codes in T_K_AVLEVERING_STATUS
 */
public enum AvleveringStatusCode {

	/**
	 * Klar for avlevering
	 */
	KLAR_FOR_AVLEVERING,

	/**
	 * Overført til arkivverket
	 */
	AVLEVERING_OVERFOERT,

	/**
	 * Avlevert og godkjent av arkivverket
	 */
	AVLEVERT,

	/**
	 * Avbrutt og skal ikke avleveres
	 */
	AVBRUTT
}
