package no.nav.dokarkiv.journalpost.v1.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LeggTilLogiskVedleggResponse {
    @Schema(
            description = "IDen til det logiske vedlegget som har blitt lagt til",
			requiredMode = REQUIRED,
			example = "1234578"
	)
    private String logiskVedleggId;
}
