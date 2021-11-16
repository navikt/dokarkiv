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
public class Tilleggsopplysning {
	@NotNull(message = "Tilleggsopplysning mangler nokkel")
	@ApiModelProperty(
			value = "Nøkkelen til det fagspesifikke attributtet.",
			required = true,
			example = "bucid"
	)
	private String nokkel;

	@NotNull(message = "Tilleggsopplysning mangler verdi")
	@ApiModelProperty(
			value = "Verdien til det fagspesifikke attributtet.",
			required = true,
			example = "12345"
	)
	private String verdi;
}