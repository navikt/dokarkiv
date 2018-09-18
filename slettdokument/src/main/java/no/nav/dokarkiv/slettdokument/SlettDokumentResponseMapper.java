package no.nav.dokarkiv.slettdokument;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;

public class SlettDokumentResponseMapper {

	public static SlettDokumentResponse mapToSlettDokumentResponse(Journalpost journalpost, DokumentInfo dokumentInfo) {
		return SlettDokumentResponse.builder()
				.tittel(dokumentInfo.getTittel())
				.dokumentInfoId(dokumentInfo.getDokumentInfoId())
				.journalStatus(journalpost.getJournalstatus() == null ? null : journalpost.getJournalstatus().name())
				.journalpostId(journalpost.getJournalpostId())
				.journalpostType(journalpost.getJournalposttype() == null ? null : journalpost.getJournalposttype().name())
				.tema(journalpost.getFagomrade() == null ? null : journalpost.getFagomrade().name())
				.build();
	}
}
