package no.nav.dokarkiv.journalpost.v1.api.finnMottatteJournalposter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FinnMottatteJournalposterResponse {

	// TODO: Sjekk at dataType="List" framleis viser i grensesnittet
	@NotNull(message = "FinnMottatteJournalposterResponse mangler Journalposter")
	@Schema(
			description = "journalposter",
			required = true
	)
	private List<UbehandletJournalpost> journalposter;
}
