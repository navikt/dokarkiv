package no.nav.dokarkiv.journalpost.v1.api.knyttTilAnnenSak;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import no.nav.dokarkiv.journalpost.v1.api.Bruker;

import java.util.List;

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
    @Schema(description = "Liste over dokumentene som skal kopieres over til ny journalpost.", name = "dokumenter", example = "[\"12345678\", \"09876543\"]")
    List<Dokument> dokumenter;

    @JsonIgnore
    public String getLogFriendlyString(){
        return "sakstype=%s, fagsakId=%s, fagsaksystem=%s, tema=%s, journalførendeEnhet=%s, dokumenter=%s".formatted(sakstype, fagsakId, fagsaksystem, tema, journalfoerendeEnhet, dokumenter);
    }
}
