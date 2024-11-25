package no.nav.dokarkiv.journalpost.v1.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

/**
 * @author Olav Røstvold Thorsen, Visma Consulting.
 */

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DokumentVedlegg {

	@Schema(
			description = "ID til journalpost kilden i Joark",
			requiredMode = REQUIRED,
			example = "22345678"
	)
	private Long kildeJournalpostId;

	@Schema(
			description = "ID til dokumentet som skal legges til som vedlegg",
			requiredMode = REQUIRED,
			example = "12345678"
	)
	private String dokumentInfoId;

}
