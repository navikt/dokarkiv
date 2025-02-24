package no.nav.dokarkiv.journalpost.v1.api.finnMottatteJournalposter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UbehandletBruker {

	@Schema(
			description = "ID til bruker i Joark",
			example = "22345678"
	)
	private String id;

	@Schema(
			description = "Brukertype i Joark",
			example = "PERSON"
	)
	private BrukerTypeCode type;
}