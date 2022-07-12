package no.nav.dokarkiv.journalpost.v1.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@Schema(description = "KnyttTilAnnenSakRequest")
public class KnyttTilAnnenSakRequest {
    @Schema(description = "Konkret fagsak i et fagsystem, FAGSAK eller GENERELL_SAK", name = "sakstype", example = "FAGSAK", allowableValues = {"FAGSAK", "GENERELL_SAK"})
    String sakstype;
    @Schema(description = "Iden til fagsaken i fagsystemet", name = "fagsakId", example = "0123A21")
    String fagsakId;
    @Schema(description = "Fagsystemet som saken behandles i", name = "fagsaksystem", example = "IT01")
    String fagsaksystem;
    @Schema(description = "Tema for saken som journalposten skal knyttes til", name = "tema", example = "SYK")
    String tema;
    @Schema(description = "Bruker", name = "bruker")
    Bruker bruker;
    @Schema(description = "NAV-enheten som personen som utfører journalføring jobber for", name = "journalfoerendeEnhet", example = "9999")
    String journalfoerendeEnhet;
}
