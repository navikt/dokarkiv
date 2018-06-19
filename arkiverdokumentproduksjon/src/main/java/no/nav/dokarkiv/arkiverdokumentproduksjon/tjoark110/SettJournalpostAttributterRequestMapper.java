package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark110;

import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.ApplicationException;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.SettJournalpostAttributterRequest;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public class SettJournalpostAttributterRequestMapper {
	public SettJournalpostAttributterRequestTo map(SettJournalpostAttributterRequest request) {
		if(request.getJournalpostIdListe() == null || request.getJournalpostIdListe().isEmpty()) {
			throw new ApplicationException("journalpostIdListe is null or empty");
		}
		return SettJournalpostAttributterRequestTo.builder()
				.journalpostIds(request.getJournalpostIdListe())
				.datoSendtPrint(request.getDatoSendt() == null ? null : request.getDatoSendt().toGregorianCalendar().getTime())
				.endretAvNavn(request.getEndretAvNavn())
				.antallRetur(request.getAntallReturpost())
				.build();
	}
}
