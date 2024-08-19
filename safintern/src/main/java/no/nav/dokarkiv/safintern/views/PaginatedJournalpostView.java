package no.nav.dokarkiv.safintern.views;

import java.util.List;

public record PaginatedJournalpostView(
		List<JournalpostView> journalposter,
		int antallRader,
		long totaltAntallRader,
		int page,
		int totalPages,
		String nextPage) {
}
