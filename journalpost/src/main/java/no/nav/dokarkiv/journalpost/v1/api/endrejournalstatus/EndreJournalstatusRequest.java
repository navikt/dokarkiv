package no.nav.dokarkiv.journalpost.v1.api.endrejournalstatus;

import io.swagger.v3.oas.annotations.media.Schema;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

public record EndreJournalstatusRequest(
		@Schema(
				description = "Ny status som skal settes på Journalposten",
				requiredMode = REQUIRED,
				allowableValues = {"MOTTATT", "UKJENT_BRUKER", "UTGAAR"}
		)
		String statusEndresTil) {
}
