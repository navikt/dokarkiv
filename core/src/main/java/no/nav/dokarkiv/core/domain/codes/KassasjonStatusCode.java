package no.nav.dokarkiv.core.domain.codes;

/**
 * Enum for codes in T_K_KASSASJON_STATUS.
 */
public enum KassasjonStatusCode {
	/*
	 * Sakens bevaringstid er passert og kan avleveres
	 */
	BEVARINGSTID_PASSERT,

	/*
	 * Sakens bevaringstid er passert og kan avleveres, og kassering av dokumenter på saken er bestilt
	 */
	BEVARINGSTID_PASSERT_DOK_KASSASJON_BESTILT,

	/*
	 * Sakens bevaringstid er passert og kan avleveres, og dokumenter på saken er kassert
	 */
	BEVARINGSTID_PASSERT_DOK_KASSERT,

	/*
	 * Saken er avlevert og kan kasseres
	 */
	KLAR_FOR_KASSASJON,

	/*
	 * Saken er kassert
	 */
	KASSERT
}