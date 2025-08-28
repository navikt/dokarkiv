package no.nav.dokarkiv.journalpost.v1.api.oppdaterJournalpostType;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import no.nav.dokarkiv.journalpost.v1.api.JournalpostType;

@Builder
@Getter
@AllArgsConstructor
public class OppdaterJournalpostTypeRequest {

	private final String typeEndresTil;
	private final String journalfoerendeEnhet;
}
