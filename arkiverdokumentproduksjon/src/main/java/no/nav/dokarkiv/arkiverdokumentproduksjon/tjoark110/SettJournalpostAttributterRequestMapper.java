package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark110;

import static java.lang.String.format;

import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.SettJournalpostAttributterRequest;
import org.springframework.stereotype.Component;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class SettJournalpostAttributterRequestMapper {
	public SettJournalpostAttributterRequestTo map(SettJournalpostAttributterRequest request) {
		if (request.getJournalpostIdListe() == null || request.getJournalpostIdListe().isEmpty()) {
			throw new ApplicationException("journalpostIdListe is null or empty");
		}
		return SettJournalpostAttributterRequestTo.builder()
				.journalpostIds(request.getJournalpostIdListe())
				.datoSendtPrint(request.getDatoSendt() == null ? null : request.getDatoSendt().toGregorianCalendar().getTime())
				.endretAvNavn(request.getEndretAvNavn())
				.antallRetur(request.getAntallReturpost())
				.utsendingskanal(mapUtsendingskanal(request.getUtsendingskanal()))
				.build();
	}

	private UtsendingsKanalCode mapUtsendingskanal(String utsendingskanal) {
		if (utsendingskanal == null || utsendingskanal.isEmpty()) {
			return null;
		} else {
			try {
				return UtsendingsKanalCode.valueOf(utsendingskanal);
			} catch (IllegalArgumentException e) {
				throw new ApplicationException(format("Ugyldig input: Utsendingkanal må være en gyldig UtsendingsKanalCode. Fikk utsendingskanal=%s", utsendingskanal));
			}
		}
	}
}
