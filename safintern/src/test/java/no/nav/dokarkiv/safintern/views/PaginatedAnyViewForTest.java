package no.nav.dokarkiv.safintern.views;

import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.InnsynCode;

import java.time.ZonedDateTime;
import java.util.List;

public record PaginatedAnyViewForTest(
		List<MinimalViableJournalpostForTest> journalposter,
		int page,
		int totalPages,
		int antallRader,
		long totaltAntallRader,
		String nextPage) {

	public record MinimalViableJournalpostForTest(
			long journalpostId,
			List<MinimalViableDokumentinfoForTest> dokumenter,
			InnsynCode innsyn,
			String innsynsbeskrivelse,
			MinimalRelevanteDatoer relevanteDatoer
	) {
	}

	public record MinimalViableDokumentinfoForTest(
			long dokumentInfoId,
			DokumentKategoriCode kategori,
			Boolean sensitivt) {
	}

	public record MinimalRelevanteDatoer(String lest) {}
}
