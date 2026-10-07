package no.nav.dokarkiv.journalpost.v1.services;

import no.nav.dokarkiv.core.domain.entities.Journalpost;

import java.util.List;

public record OpprettJournalpostResult(
		boolean alleredeOpprettet,
		Long journalpostId,
		String journalStatus,
		String journalForendeEnhetId,
		List<Long> dokumentInfoIds
) {
	public static OpprettJournalpostResult fra(boolean alleredeOpprettet, Journalpost journalpost) {
		return new OpprettJournalpostResult(alleredeOpprettet,
				journalpost.getJournalpostId(),
				journalpost.getJournalstatus().name(),
				journalpost.getJournalForendeEnhetId(),
				journalpost.getJournalpostDokumentInfoRelasjoner()
						.stream()
						.map(rel -> rel.getDokumentInfo().getDokumentInfoId())
						.toList());
	}
}