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
public class EndreLogiskVedleggRequest {
    @NotNull(message = "EndreLogiskVedleggRequest mangler tittel")
    @ApiModelProperty(value = "Den nye tittelen til det logiske vedlegget, for eksempel \"Kontoutskrift\".",
                      example = "Kontoutskrift",
                      required = true)
    private String tittel;
}
