package no.nav.dokarkiv.journalpost.v1.api;


import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FjernVedleggTilknyttetJournalpostRequest {

	@ApiModelProperty(
			value = "DokumentinfoId som har vedlegg knyttet journalpost.",
			required = true,
			example = "12345678")
	private String dokumentId;
}
