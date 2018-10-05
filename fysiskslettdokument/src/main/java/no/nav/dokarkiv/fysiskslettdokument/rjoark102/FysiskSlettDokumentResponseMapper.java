package no.nav.dokarkiv.fysiskslettdokument.rjoark102;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;

//MÅ ENDRES NÅR LOGIKK ER PÅ PLASS
public class FysiskSlettDokumentResponseMapper {

	public static FysiskSlettDokumentResponse mapToFysiskSlettDokumentResponse(Journalpost journalpost, DokumentInfo dokumentInfo) {
		return FysiskSlettDokumentResponse.builder()
				.tittel(dokumentInfo.getTittel())
				.dokumentInfoId(dokumentInfo.getDokumentInfoId())
				.slettet(dokumentInfo.getSlettet())
				.journalStatus(journalpost.getJournalstatus() == null ? null : journalpost.getJournalstatus().name())
				.journalpostId(journalpost.getJournalpostId())
				.journalpostType(journalpost.getJournalposttype() == null ? null : journalpost.getJournalposttype().name())
				.tema(journalpost.getFagomrade() == null ? null : journalpost.getFagomrade().name())
				.build();
	}
}
