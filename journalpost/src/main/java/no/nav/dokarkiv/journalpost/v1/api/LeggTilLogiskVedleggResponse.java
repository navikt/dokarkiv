package no.nav.dokarkiv.journalpost.v1.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@ApiModel
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LeggTilLogiskVedleggResponse {
    @ApiModelProperty(
            value = "IDen til det logiske vedlegget som har blitt lagt til",
            required = true)
    private String logiskVedleggId;
}
