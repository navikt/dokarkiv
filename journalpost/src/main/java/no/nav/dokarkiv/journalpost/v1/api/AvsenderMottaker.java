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
public class AvsenderMottaker {
    @ApiModelProperty(
            value = "Fødselsnummer/d-nummer (11 siffer) eller organisasjonsnummer (9 siffer)",
            required = false)
    private String id;

    @NotNull(message = "AvsenderMottaker mangler navn")
    @ApiModelProperty(
            value = "Navn på personbrukere skal lagres på formatet etternavn, fornavn mellomnavn",
            required = true)
    private String navn;

    @ApiModelProperty(
            value = "",
            required = false)
    private String land;
}
