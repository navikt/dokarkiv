package no.nav.dokarkiv.journalpost.v1.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LeggTilLogiskVedleggRequest {
	@NotNull(message = "tittel kan ikke være null")
	@Schema(
			description = """
					Tittelen som det nye logiske vedlegget skal ha, for eksempel "Kontoutskrift".
					""",
			requiredMode = REQUIRED,
			example = "Kontoutskrift"
	)
	private String tittel;
}
