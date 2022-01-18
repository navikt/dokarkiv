package no.nav.dokarkiv.journalpost.v1.api;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FjernVedleggTilknyttetJournalpostRequest {

	@Schema(
			description = "DokumentinfoId som har vedlegg knyttet journalpost.",
			required = true,
			example = "12345678"
	)
	private String dokumentId;
}
