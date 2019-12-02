package no.nav.dokarkiv.journalpost.v1.api;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OppdaterJournalpostRequest {
    @ApiModelProperty(value = "Avsender eller mottaker av forsendelsen.\n\nSkal ikke settes for notater.")
    private AvsenderMottaker avsenderMottaker;

    @ApiModelProperty(value = "Brukeren som forsendelsen gjelder.")
    private Bruker bruker;

    @ApiModelProperty(value = "Saken i PSAK eller GSAK som dokumentene skal journalføres mot.\nNB: Dersom journalposten tilhører en fagsak i et fagsystem, må konsument selv sørge for å opprette en GSAK-sak med mapping til fagsaken. Alternativt kan fagsystemet benytte tjenesten knyttTilSak, som knytter journalposten til en fagsak eller generell sak.")
    private Sak sak;

    @ApiModelProperty(
            value = "Fagområdet som forsendelsen tilhører, for eksempel \"FOR\" for Foreldrepenger",
            example = "FOR")
    private String tema;

    @ApiModelProperty(
            value = "Behandlingstema for forsendelsen, for eksempel ab0001 (Ordinære dagpenger).",
            example = "ab0001"
    )
    private String behandlingstema;

    @ApiModelProperty(
            value = "Tittel som beskriver forsendelsen samlet, for eksempel \"Ettersendelse til søknad om foreldrepenger\"",
            example = "Ettersendelse til søknad om foreldrepenger"
    )
    private String tittel;

    @ApiModelProperty(
            value = "NAV-enheten som har journalført, eventuelt skal journalføre, forsendelsen. Ved automatisk journalføring uten mennesker involvert skal enhet settes til \"9999\".",
            example = "9999"
    )
    private String journalfoerendeEnhet;

    @ApiModelProperty(value = "Dato forsendelsen ble mottatt i retur. Feltet kan kun settes for utgående journalposter.")
    @JsonFormat(pattern="yyyy-MM-dd")
    private Date datoRetur;

    @ApiModelProperty(
            value = "Dato forsendelsen ble mottatt fra avsender. Feltet kan kun settes for inngående journalposter.",
            dataType = "LocalDate",
            example = "2019-11-29")
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate datoMottatt;

    @ApiModelProperty(value = "Fagsystemene som arkiverer kan legge til egne fagspesifikke attributter per journalpost. Disse er representert som et skjemaløst nøkkel-verdi-sett og valideres ikke ved arkivering. Et eksempel på et slikt sett kan være nøkkel: bucid og verdi: 21521.")
    private List<Tilleggsopplysning> tilleggsopplysninger;

    @ApiModelProperty(value = "Liste over dokumentene på journalposten der metadata skal endres")
    private List<DokumentInfo> dokumenter;
}
