package no.nav.dokarkiv.journalpost.v1.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Dokument {

	@ApiModelProperty(
			value = "",
			required = false)
	private String tittel;

	@ApiModelProperty(
			value = "",
			required = false)
	private String brevkode;

	@ApiModelProperty(
			value = "",
			required = false)
	private String dokumentKategori;

	@ApiModelProperty(
			value = "",
			required = true)
	private List<DokumentVariant> dokumentvarianter = new ArrayList<>();
}
