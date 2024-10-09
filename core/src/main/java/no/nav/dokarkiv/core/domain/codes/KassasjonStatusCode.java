package no.nav.dokarkiv.core.domain.codes;

/**
 * Enum for codes in T_K_KASSASJON_STATUS.
 */
public enum KassasjonStatusCode {
	/*
	 * Saken har nådd kassasjonstid
	 */
	KASSASJONSTID_NAADD,

	/*
	 * Saken kan kasseres
	 */
	KLAR_FOR_KASSASJON,

	/*
	 * Dokumenter på saken er kassert
	 */
	DOKUMENTER_KASSERT

}
