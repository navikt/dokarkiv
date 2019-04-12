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
			value = "",
			required = true)
	private String nokkel;

	@NotNull(message = "Tilleggsopplysning mangler verdi")
	@ApiModelProperty(
			value = "",
			required = true)
	private String verdi;
}