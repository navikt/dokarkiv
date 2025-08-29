package no.nav.dokarkiv.journalpost.v1.api.oppdaterJournalposttype;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class OppdaterJournalposttypeRequest {

	private final String typeEndresTil;
	private final String journalfoerendeEnhet;
}
