package no.nav.dokarkiv.logiskslettdokument.rjoark100;

import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;


public class LogiskSlettDokumentResponseMapper {

	public static LogiskSlettDokumentResponse mapToSlettDokumentResponse(Journalpost journalpost, DokumentInfo dokumentInfo) {
		return LogiskSlettDokumentResponse.builder()
				.tittel(dokumentInfo.getTittel())
				.dokumentInfoId(dokumentInfo.getDokumentInfoId())
				.journalStatus(journalpost.getJournalstatus() == null ? null : journalpost.getJournalstatus().name())
				.journalpostId(journalpost.getJournalpostId())
				.journalpostType(journalpost.getJournalposttype() == null ? null : journalpost.getJournalposttype().name())
				.tema(journalpost.getFagomrade() == null ? null : journalpost.getFagomrade().name())
                .slettet(journalpost.isBegrenset(BegrensningTypeCode.UTILGJENGELIGGJORT))
				.build();
	}
}
