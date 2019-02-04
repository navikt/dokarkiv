package no.nav.dokarkiv.logiskslettdokument.rjoark100;

import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;


public final class LogiskSlettDokumentResponseMapper {

	private LogiskSlettDokumentResponseMapper() {
		throw new IllegalStateException("Utility class");
	}

	public static LogiskSlettDokumentResponse mapToSlettDokumentResponse(JournalpostDokumentInfoRelasjon relasjon) {
		return LogiskSlettDokumentResponse.builder()
				.tittel(relasjon.getDokumentInfo().getTittel())
				.dokumentInfoId(relasjon.getDokumentInfo().getDokumentInfoId())
				.journalpostId(relasjon.getJournalpost().getJournalpostId())
				.tilknyttetJournalpostSom(relasjon.getTilknyttetJournalpostSom().name())
				.build();
	}
}
