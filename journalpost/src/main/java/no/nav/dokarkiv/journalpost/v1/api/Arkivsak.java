package no.nav.dok.oppdaterjournalpost.api.v1;

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
public class Arkivsak {
    @NotNull(message = "Arkivsak mangler arkivsaksnummer")
    @ApiModelProperty(
            value = "Angir hvorvidt arkivsaken befinner seg i GSAK (FS22) eller PSAK (PEN)",
            required = true)
    private String arkivsaksnummer;

    @NotNull(message = "Arkivsak mangler arkivsaksystem")
    @ApiModelProperty(
            value = "Fagområdet som forsendelsen tilhører, for eksempel \"FOR\" for Foreldrepenger",
            required = true)
    private Arkivsaksystem arkivsaksystem;
}
