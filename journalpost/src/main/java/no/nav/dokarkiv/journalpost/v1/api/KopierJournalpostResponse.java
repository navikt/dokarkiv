package no.nav.dokarkiv.journalpost.v1.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KopierJournalpostResponse {

	@Schema(
			description = "journalpostId som har blitt kopiert",
			example = "453865158"
	)
	private String kopierJournalpostId;
}
