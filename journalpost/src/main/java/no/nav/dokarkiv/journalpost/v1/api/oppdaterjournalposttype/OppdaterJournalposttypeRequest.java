package no.nav.dokarkiv.journalpost.v1.api.oppdaterjournalposttype;

import no.nav.dokarkiv.journalpost.v1.api.JournalpostType;

public record OppdaterJournalposttypeRequest(JournalpostType typeEndresTil,
											 String journalfoerendeEnhet) {
}
