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
public class DokumentVariant {

	@NotNull(message = "Filtype kan ikke være null")
	@ApiModelProperty(
			value = "Filtypen til filen som følger, feks PDF/A, JSON eller XML.",
			required = true,
			example = "PDF/A"
	)
	private String filtype;

	@NotNull(message = "Variantformat kan ikke være null")
	@ApiModelProperty(
			value = "ARKIV brukes for dokumentvarianter i menneskelesbart format (for eksempel PDF/A).  Gosys og nav.no henter arkivvariant og viser denne til bruker.\n" +
					"ORIGINAL skal brukes for dokumentvariant i maskinlesbart format (for eksempel XML og JSON) som brukes for automatisk saksbehandling\n" +
					"Alle dokumenter må ha én variant med variantFormat ARKIV.",
			required = true,
			example = "ARKIV"
	)
	private String variantformat;

	@ApiModelProperty(
			value = "Selve dokumentet",
			required = false)
	private byte[] fysiskDokument;
}
