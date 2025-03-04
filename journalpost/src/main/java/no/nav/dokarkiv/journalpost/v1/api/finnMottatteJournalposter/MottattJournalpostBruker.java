package no.nav.dokarkiv.journalpost.v1.api.finnMottatteJournalposter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MottattJournalpostBruker {

	@Schema(
			description = "Brukerens fødselsnummer (11 siffer), aktørID (13 siffer) eller organisasjonsnummer (9 siffer)",
			requiredMode = REQUIRED,
			example = "01117400200"
	)
	private String id;

	@Schema(
			description = "Angir hvilken type identifikator som er benyttet i `bruker.id`",
			requiredMode = REQUIRED,
			example = "FNR"
	)
	private String type;
}