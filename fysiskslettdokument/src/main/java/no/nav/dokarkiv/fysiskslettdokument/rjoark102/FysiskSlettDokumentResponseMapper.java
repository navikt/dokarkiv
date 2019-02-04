package no.nav.dokarkiv.fysiskslettdokument.rjoark102;

import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;

final class FysiskSlettDokumentResponseMapper {

	private FysiskSlettDokumentResponseMapper() {
		throw new IllegalStateException("Utility class");
	}

	public static FysiskSlettDokumentResponse mapToFysiskSlettDokumentResponse(
			JournalpostDokumentInfoRelasjon relasjon) {
		return FysiskSlettDokumentResponse.builder()
				.journalpostId(relasjon.getJournalpost().getJournalpostId())
				.dokumentInfoId(relasjon.getDokumentInfo().getDokumentInfoId())
				.tittel(relasjon.getDokumentInfo().getTittel())
				.tilknyttetJournalpostSomCode(relasjon.getTilknyttetJournalpostSom())
				.build();
	}
}
