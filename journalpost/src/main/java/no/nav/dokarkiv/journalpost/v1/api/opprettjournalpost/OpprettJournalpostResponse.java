package no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@ApiModel
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OpprettJournalpostResponse {

    @ApiModelProperty(
            value = "JournalpostId som har blitt opprettet",
            required = true,
            position = 1,
            example = "\"467010363\""
    )
    private String journalpostId;

    /**
     * @deprecated Skal ikke brukes lenger. Bruk journalpostferdigstilt istedenfor
     */
    @ApiModelProperty(
            value = "Journalstatus for journalpost.\n" +
                    "* MIDLERTIDIG - hvis journalpost er opprettet\n" +
                    "* ENDELIG - hvis journalpost er opprett og endelig journalført\n\n " +
                    "Feltet er deprekert og vil bli fjernet i fremtiden. Bruk journalpostferdigstilt i stedet.",
            required = true,
            hidden = true,
            example = "ENDELIG")
    @Deprecated
    private String journalstatus;

    @ApiModelProperty(
            value = "Melding",
            hidden = true,
            example = "null"
    )
    private String melding;

    @ApiModelProperty(
            value = "True eller False for om journalpost ble ferdigstilt",
            position = 3,
            example = "true"
    )
    private Boolean journalpostferdigstilt;

    @ApiModelProperty(
            value = "Dokumentene på journalposten.",
            position = 4
    )
    private List<DokumentInfoId> dokumenter;
}
