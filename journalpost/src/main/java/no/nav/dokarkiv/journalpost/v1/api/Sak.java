package no.nav.dokarkiv.journalpost.v1.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Sak {
    @ApiModelProperty(
            value = "* FAGSAK vil si at dokumentene tilhører en sak i et fagsystem. Dersom FAGSAK velges, må fagsakid og fagsaksystem oppgis.\n" +
                    "* GENERELL_SAK kan brukes for dokumenter som skal journalføres, men som ikke tilhører en konkret fagsak. Generell sak kan ses på som brukerens \"mappe\" på et gitt tema.\n" +
                    "* ARKIVSAK skal kun brukes etter avtale.",
            example = "FAGSAK"
    )
    private Sakstype sakstype;

    @ApiModelProperty(
            value = "Iden til fagsaken i fagsystemet (altså ikke applikasjonen SAK).\n" +
                    "Skal kun settes dersom sakstype = FAGSAK",
            example = "\"10695768\""
    )
    private String fagsakId;

    @ApiModelProperty(
            value = "Fagsystemet som saken behandles i. Lovlige verdier er \n" +
                    "* FS38 (Melosys)\n" +
                    "* FS36 (Foreldrepengeløsningen)\n" +
                    "* UFM (Unntak fra medlemskap)\n" +
                    "* AO01 (Arena)\n" +
                    "* AO11 (Grisen)\n" +
                    "* IT01 (Infotrygd)\n" +
                    "* OEBS\n" +
                    "* PP01\n" +
                    "* PP01\n" +
                    "* K9\n" +
                    "* BISYS\n" +
                    "* BA (Barnetrygd)\n" +
                    "* EF (Enslig forsørger)\n" +
                    "* KONT (Kontantstøtte)\n" +
                    "Skal kun settes dersom sakstype = FAGSAK",
            example = "AO01"
    )
    private Fagsaksystem fagsaksystem;

    @ApiModelProperty(
            value = "Saksnummeret i PSAK eller GSAK (SAK). Må være et numerisk heltall.\n" +
                    "Skal kun settes dersom sakstype = ARKIVSAK.\n" +
                    "Feltet skal kun brukes etter avtale. ",
            hidden = true
    )
    @Deprecated
    private String arkivsaksnummer;

    @ApiModelProperty(
            value = "Skal kun settes dersom sakstype = ARKIVSAK.\n" +
                    "Feltet skal kun brukes etter avtale. GSAK,PSAK",
            hidden = true
    )
    @Deprecated
    private Arkivsaksystem arkivsaksystem;
}
