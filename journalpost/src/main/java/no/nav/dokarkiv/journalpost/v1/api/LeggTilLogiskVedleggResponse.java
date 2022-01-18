package no.nav.dokarkiv.journalpost.v1.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LeggTilLogiskVedleggResponse {
    @Schema(
            description = "IDen til det logiske vedlegget som har blitt lagt til",
			required = true,
			example = "1234578"
	)
    private String logiskVedleggId;
}
