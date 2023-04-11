package no.nav.dokarkiv.core.domain.codes;

/**
 * Enum for codes in T_K_SAK_STATUS.
 */
public enum SakStatusCode {

	/**
	 * Saken er opprettet og åpen/aktiv. Nye journalposter kan knyttes til saken.
	 */
	AAPEN,

	/**
	 * Saken er avsluttet.
	 */
	AVSLUTTET,

	/**
	 * Kassasjonsregler for saken er oppfylt.
	 */
	KAN_KASSERES,

	/**
	 * Dokumenter på journalposter som er knyttet til saken, og som kan kasseres, er kassert (slettet).
	 */
	KASSERT,

	/**
	 * Saken er klar for avlevering.
	 */
	KAN_AVLEVERES,

	/**
	 * Saken er avlevert til arkivverket, og vi venter på godkjenning.
	 */
	AVLEVERT_GODKJENNING,

	/**
	 * Saken er avlevert til arkivverket.
	 */
	AVLEVERT,

	/**
	 * Saken kan slettes.
	 */
	KAN_SLETTES
}
