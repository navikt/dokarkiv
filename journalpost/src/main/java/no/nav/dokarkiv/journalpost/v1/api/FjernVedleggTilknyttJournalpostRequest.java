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
public class FjernVedleggTilknyttJournalpostRequest {

	@ApiModelProperty(
			value = "DokumentinfoId som skal knyttes til journalpostId som vedlegg.",
			required = true,
			example = "123456789")
	private String dokumentId;
}
