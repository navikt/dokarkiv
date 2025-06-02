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
     * DSOP Kontroll
     */
    ARGUS,
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
     * Fagsystem for helsetjenester
     */
    HELT,
    /**
     * Fagsystem for å saksbehandling av ungdomsprogramytelsen
     */
    UNG_SAK
}
