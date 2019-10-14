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
public class Sak {
    @NotNull(message = "Sak mangler arkivsaksnummer")
    @ApiModelProperty(
            value = "Saksnummeret i PSAK eller GSAK (SAK). Må være et numerisk heltall.",
            required = true,
            example = "111111111"
    )
    private String arkivsaksnummer;

    @NotNull(message = "Sak mangler arkivsaksystem")
    @ApiModelProperty(
            value = "\"PSAK\" skal brukes for saker som behandles i Pesys\n\"GSAK\" skal brukes for alle andre sakstyper.",
            required = true,
            example = "GSAK"
    )
    private Arkivsaksystem arkivsaksystem;
}
