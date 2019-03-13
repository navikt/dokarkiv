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
            value = "",
            required = true)
    private BrukerIdType idType;

    @NotNull(message = "Bruker mangler id")
    @ApiModelProperty(
            value = "",
            required = true)
    private String id;
}
