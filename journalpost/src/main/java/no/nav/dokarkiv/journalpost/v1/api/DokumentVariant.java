package no.nav.dokarkiv.journalpost.v1.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DokumentVariant {

	@ApiModelProperty(
			value = "Filtypen til filen som følger, feks PDF/A, JSON eller XML.",
			example = "PDF/A",
			required = true)
	private String filtype;

	@ApiModelProperty(
			value = "ARKIV brukes for dokumentvarianter i menneskelesbart format (for eksempel PDF/A).  Gosys og nav.no henter arkivvariant og viser denne til bruker.\n" +
					"ORIGINAL skal brukes for dokumentvariant i maskinlesbart format (for eksempel XML og JSON) som brukes for automatisk saksbehandling\n" +
					"Alle dokumenter må ha én variant med variantFormat ARKIV.",
			example = "ARKIV",
			required = true)
	private String variantformat;

	@ApiModelProperty(
			value = "Selve dokumentet",
			required = false)
	private byte[] fysiskDokument;
}
