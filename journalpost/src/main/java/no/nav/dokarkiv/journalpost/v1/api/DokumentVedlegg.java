package no.nav.dokarkiv.journalpost.v1.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

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

	@Schema(
			description = "Vedleggets plassering blant alle vedlegg på journalposten. Skal være > 0 eller ikke satt (null)",
			requiredMode = NOT_REQUIRED,
			example = "1"
	)
	private Integer rekkefoelge;

}
