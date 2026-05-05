package no.nav.dokarkiv.journalpost.v1.api;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema
public enum Fagsaksystem {
    FS38,
    FS36,
    UFM,
    OEBS,
    OB36,
    AO01,
    AO11,
    IT01,
    PP01,
    K9,
    BISYS,
    BA,
    EF,
    KONT,
    SUPSTONAD,
    OMSORGSPENGER,
    HJELPEMIDLER,
    BARNEBRILLER,
    EY,
    /**
     * Produktområde AAP
     */
    KELVIN,
    DAGPENGER,

    /**
     * Yrkesskade
     */
    KOMPYS,

	/**
	 * WATSON - fagsystem for Nav Kontroll
	 */
	WATSON,
    NEESSI,
    TILLEGGSSTONADER,
    ARBEIDSOPPFOLGING,
    TILTAKSPENGER,
    TILTAKSADMINISTRASJON,
    /**
     * Inkluderende arbeidsliv
     */
    FIA,
    /**
     * Fagsystem for ortopediske hjelpemidler
     */
    HELT,
    /**
     * Fagsystem for diverse hjelpemidler
     */
    SUPERHELT,
    /**
     * Fagsystem for å saksbehandling av ungdomsprogramytelsen
     */
    UNG_SAK,
    /**
     * Identstyring, ifm avvikling av Remedy migreres dokumentasjon til Joark
     */
    REMEDY,
    /**
     * Arbeidsgiver kan sende inn søknad om tilskudd til EKSPERTBISTAND og se svaret på søknaden.
     */
    EKSPERTBISTAND
}
