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
public class Tilleggsopplysning {
	@NotNull(message = "Tilleggsopplysning mangler nokkel")
	@Schema(
			description = "Nøkkelen til det fagspesifikke attributtet.",
			requiredMode = REQUIRED,
			example = "bucid"
	)
	private String nokkel;

	@NotNull(message = "Tilleggsopplysning mangler verdi")
	@Schema(
			description = "Verdien til det fagspesifikke attributtet.",
			requiredMode = REQUIRED,
			example = "12345"
	)
	private String verdi;
}