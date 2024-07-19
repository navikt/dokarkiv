package no.nav.dokarkiv.safintern.views;

import java.util.List;

public record PaginatedAnyViewForTest(
		List<MinimalViableJournalpostForTest> journalposter,
		int page,
		int totalPages,
		int antallRader,
		long totaltAntallRader,
		String nextPage) {

	public record MinimalViableJournalpostForTest(long journalpostId) {
	}
}
