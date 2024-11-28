package no.nav.dokarkiv.journalpost.v1.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EndreLogiskVedleggRequest {
	@NotNull(message = "EndreLogiskVedleggRequest mangler tittel")
	@Schema(
			description = """
					Den nye tittelen til det logiske vedlegget, for eksempel "Kontoutskrift".
					""",
			requiredMode = REQUIRED,
			example = "Kontoutskrift"
	)
	private String tittel;
}
