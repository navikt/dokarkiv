package no.nav.dokarkiv.journalpost.v1.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DokumentInfo {
    @NotNull(message = "DokumentInfo mangler dokumentInfoId")
    @ApiModelProperty(
            value = "ID til dokumentinfo-objektet i Joark",
            required = true,
            example = "\"485227498\"")
    private String dokumentInfoId;

    @ApiModelProperty(
            value = "Kode som sier noe om dokumentets innhold og oppbygning.\nFor inngående dokumenter kan brevkoden være en NAV-skjemaID f.eks. \"NAV 14-05.09\" eller en SED-id.\nBrevkode skal ikke settes for ustrukturert, uklassifisert dokumentasjon, f.eks. brukeropplastede vedlegg.\n",
            example = "NAV 14-05.09"
    )
    private String brevkode;

    @ApiModelProperty(
            value = "Tittel som beskriver dokumentet, for eksempel \"Søknad om foreldrepenger ved fødsel\".\nDokumentets tittel blir synlig i brukers journal på nav.no, samt i NAVs fagsystemer.",
            example = "Søknad om foreldrepenger ved fødsel"
    )
    private String tittel;
}
