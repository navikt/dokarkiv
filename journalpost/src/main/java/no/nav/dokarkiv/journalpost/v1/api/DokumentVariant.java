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
			value = "",
			required = true)
	private String filtype;

	@ApiModelProperty(
			value = "",
			required = true)
	private String variantformat;

	@ApiModelProperty(
			value = "",
			required = false)
	private byte[] fysiskDokument;
}
