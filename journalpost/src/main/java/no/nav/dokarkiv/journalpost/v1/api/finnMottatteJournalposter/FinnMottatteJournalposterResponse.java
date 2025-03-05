package no.nav.dokarkiv.journalpost.v1.api.finnMottatteJournalposter;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FinnMottatteJournalposterResponse {

	@ArraySchema(arraySchema = @Schema(
			description = "journalposter"
	))
	private Set<MottattJournalpost> journalposter;

}
