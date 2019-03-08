package no.nav.dokarkiv.journalpost.v1.api;


import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OppdaterJournalpostRequest {
    @ApiModelProperty(value = "Avsender eller mottaker av forsendelsen.\n\nSkal ikke settes for notater.")
    private AvsenderMottaker avsenderMottaker;

    @ApiModelProperty(value = "")
    private Bruker bruker;

    @ApiModelProperty(value = "Referanse til arkivsaken i GSAK eller PSAK som journalposten skal journalføres mot.")
    private Arkivsak arkivsak;

    @ApiModelProperty(value = "Fagområdet som forsendelsen tilhører, for eksempel \"FOR\" for Foreldrepenger")
    private String tema;

    @ApiModelProperty(value = "")
    private String behandlingstema;

    @ApiModelProperty(value = "Tittel som beskriver forsendelsen samlet, for eksempel \"Ettersendelse til søknad om foreldrepenger\".\n\nFeltet vil bli vist frem i brukers journal på nav.no, samt i Gosys og fagsystemer.")
    private String tittel;

    @ApiModelProperty(value = "")
    private String avsenderMottakerLand;

    @ApiModelProperty(value = "")
    private List<Tilleggsopplysning> tilleggsopplysninger;

    @ApiModelProperty(value = "liste over dokumentene på journalposten der metadata skal endres")
    private List<DokumentInfo> dokumentInfoList;
}
