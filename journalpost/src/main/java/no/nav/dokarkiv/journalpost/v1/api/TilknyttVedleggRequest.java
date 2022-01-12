package no.nav.dokarkiv.journalpost.v1.api;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Olav Røstvold Thorsen, Visma Consulting.
 */

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TilknyttVedleggRequest {

	@Schema(
			description = "Navn på saksbehandler som tilknytter vedleggene",
			required = true,
			example = "Mikkel Pettersen"
	)
	private String tilknyttetAvNavn;

	@Builder.Default
	@ArraySchema(arraySchema = @Schema(
			description = "Liste med et eller flere dokumenter som skal knyttes til journalpostId som vedlegg",
			required = true
		)
	)
	private List<DokumentVedlegg> dokument = new ArrayList<>();

}
