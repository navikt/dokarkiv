package no.nav.dokarkiv.core.domain.codes;

/**
 * Koder for innsyn til journalpost.
 * <p>
 * Brukes av innsynsløsningen.
 */
public enum InnsynCode {
	BRUK_STANDARDREGLER,
	VISES_MASKINELT_GODKJENT,
	VISES_MANUELT_GODKJENT,
	VISES_FORVALTNINGSNOTAT,
	SKJULES_FEILSENDT,
	SKJULES_BRUKERS_ØNSKE,
	SKJULES_ORGAN_INTERNT,
	SKJULES_INNSKRENKET_PARTSINNSYN
}
