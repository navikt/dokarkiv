package no.nav.dokarkiv.safintern.journalstatus;

import no.nav.dokarkiv.safintern.views.JournalpostView;

import java.util.List;

public record PaginatedJournalpostView(
		List<JournalpostView> journalposter,
		int antallRader,
		long totaltAntallRader,
		int page,
		int totalPages,
		String nextPage) {
}
