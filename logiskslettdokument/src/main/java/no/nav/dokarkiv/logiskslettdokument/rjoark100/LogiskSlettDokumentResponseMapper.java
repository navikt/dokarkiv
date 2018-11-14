package no.nav.dokarkiv.logiskslettdokument.rjoark100;

import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;


public class LogiskSlettDokumentResponseMapper {

	public static LogiskSlettDokumentResponse mapToSlettDokumentResponse(JournalpostDokumentInfoRelasjon relasjon) {
		return LogiskSlettDokumentResponse.builder()
				.tittel(relasjon.getDokumentInfo() == null ? null : relasjon.getDokumentInfo().getTittel())
				.dokumentInfoId(relasjon.getDokumentInfo() == null ? null : relasjon.getDokumentInfo().getDokumentInfoId())
				.journalpostId(relasjon.getJournalpost() == null ? null : relasjon.getJournalpost().getJournalpostId())
				.slettet(relasjon.getDokumentInfo() == null ? null : relasjon.getDokumentInfo().getSlettet())
				.tilknyttetJournalpostSom(relasjon.getTilknyttetJournalpostSom().name())
				.build();
	}
}
