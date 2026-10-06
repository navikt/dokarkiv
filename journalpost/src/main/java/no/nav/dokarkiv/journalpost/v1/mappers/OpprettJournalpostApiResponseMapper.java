package no.nav.dokarkiv.journalpost.v1.mappers;

import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.DokumentInfoId;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostResponse;
import no.nav.dokarkiv.journalpost.v1.services.ForsoekFerdigstillJournalpostResult;
import no.nav.dokarkiv.journalpost.v1.services.OpprettJournalpostResult;

import java.util.List;

public class OpprettJournalpostApiResponseMapper {
	public static OpprettJournalpostResponse mapMedForsoekFerdigstill(
			long journalpostId,
			OpprettJournalpostResult opprettJournalpostResult,
			ForsoekFerdigstillJournalpostResult forsoekFerdigstillResult) {

		return OpprettJournalpostResponse.builder()
				.journalpostId(String.valueOf(journalpostId))
				.journalstatus(forsoekFerdigstillResult.status().getApiNavn())
				.melding(forsoekFerdigstillResult.melding())
				.journalpostferdigstilt(forsoekFerdigstillResult.status().isFerdigstilt())
				.dokumenter(mapDokumenter(opprettJournalpostResult))
				.build();
	}

	public static OpprettJournalpostResponse mapUtenForsoekFerdigstill(
			long journalpostId,
			OpprettJournalpostResult opprettJournalpostResult) {

		return OpprettJournalpostResponse.builder()
				.journalpostId(String.valueOf(journalpostId))
				.journalstatus(opprettJournalpostResult.journalStatus())
				.melding(null)
				.journalpostferdigstilt(false)
				.dokumenter(mapDokumenter(opprettJournalpostResult))
				.build();
	}

	private static List<DokumentInfoId> mapDokumenter(OpprettJournalpostResult opprettJournalpostResult) {
		return opprettJournalpostResult.dokumentInfoIds()
				.stream()
				.map(id -> new DokumentInfoId(id.toString()))
				.toList();
	}
}
