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
public class Bruker {
    @NotNull(message = "Bruker mangler idType")
    @ApiModelProperty(
            value = "Angir hvilken type identifikator som er benyttet i bruker.id",
            required = true,
            example = "FNR"
    )
    private BrukerIdType idType;

    @NotNull(message = "Bruker mangler id")
    @ApiModelProperty(
            value = "Brukerens fødselsnummer (11 siffer) eller organisasjonsnummer (9 siffer)",
            required = true,
            example = "***gammelt_fnr***"
    )
    private String id;
}
