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
            required = true)
    private String dokumentInfoId;

    @ApiModelProperty(
            value = "ID som beskriver typen dokument, for eksempel \"NAV 14-05.09\" (Søknad om foreldrepenger ved fødsel)",
            required = false)
    private String brevkode;

    @ApiModelProperty(
            value = "Tittel som beskriver dokumentet, for eksempel \"Søknad om foreldrepenger ved fødsel\"",
            required = false)
    private String tittel;

}
