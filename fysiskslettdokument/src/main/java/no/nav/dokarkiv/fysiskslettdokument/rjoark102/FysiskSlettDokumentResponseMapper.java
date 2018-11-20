package no.nav.dokarkiv.fysiskslettdokument.rjoark102;

import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;

public class FysiskSlettDokumentResponseMapper {

	public static FysiskSlettDokumentResponse mapToFysiskSlettDokumentResponse(
			JournalpostDokumentInfoRelasjon relasjon) {
		return FysiskSlettDokumentResponse.builder()
				.journalpostId(relasjon.getJournalpost().getJournalpostId())
				.dokumentInfoId(relasjon.getDokumentInfo().getDokumentInfoId())
				.tittel(relasjon.getDokumentInfo().getTittel())
				.tilknyttetJournalpostSomCode(relasjon.getTilknyttetJournalpostSom())
				.slettet(relasjon.getDokumentInfo().getSlettet())
				.build();
	}
}
