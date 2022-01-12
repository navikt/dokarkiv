package no.nav.dokarkiv.journalpost.v1.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LeggTilLogiskVedleggRequest {
	@NotNull(message = "EndreLogiskVedleggRequest mangler tittel")
	@Schema(
			description = """
					Tittelen som det nye logiske vedlegget skal ha, for eksempel "Kontoutskrift".
					""",
			required = true,
			example = "Kontoutskrift"
	)
	private String tittel;
}
