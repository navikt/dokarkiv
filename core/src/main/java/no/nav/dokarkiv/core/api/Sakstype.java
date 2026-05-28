package no.nav.dokarkiv.core.api;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema
public enum Sakstype {
    FAGSAK,
    GENERELL_SAK,
    @Deprecated ARKIVSAK
}
