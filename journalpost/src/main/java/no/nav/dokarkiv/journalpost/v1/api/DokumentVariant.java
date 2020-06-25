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
public class DokumentVariant {

    @NotNull(message = "Filtype kan ikke være null")
    @ApiModelProperty(
            value = "Filtypen til filen som følger, feks PDFA, JSON eller XML.",
            required = true,
            example = "PDFA"
    )
    private String filtype;

    @NotNull(message = "Variantformat kan ikke være null")
    @ApiModelProperty(
            value = "ARKIV brukes for dokumentvarianter i menneskelesbart format (for eksempel PDFA).  Gosys og nav.no henter arkivvariant og viser denne til bruker.\n" +
                    "ORIGINAL skal brukes for dokumentvariant i maskinlesbart format (for eksempel XML og JSON) som brukes for automatisk saksbehandling\n" +
                    "Alle dokumenter må ha én variant med variantFormat ARKIV.",
            required = true,
            example = "ARKIV"
    )
    private String variantformat;

    @ApiModelProperty(
            value = "Selve dokumentet.  Hvis filtype er PDF/XML, ved fysisk dokument brukes bytearray.",
            required = true,
            example = "U8O4a25hZCBvbSBkYWdwZW5nZXIgdmVkIHBlcm1pdHRlcmluZw=="
    )
    private byte[] fysiskDokument;

    @ApiModelProperty(
            value = "Navnet filen skal ha i arkivet.",
            example = "eksempeldokument.pdf",
            hidden = true
    )
    private String filnavn;

    @ApiModelProperty(
            value = "Navnet på skanningsbatchen som produserte filen. Feltet skal kun brukes etter avtale",
            example = "R512345678",
            hidden = true
    )
    private String batchnavn;
}
