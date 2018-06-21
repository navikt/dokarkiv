package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark104;

import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.ApplicationException;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.SettDatoSendtRequest;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * Default implementation of SettDatoSendtRequestMapper
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@Component
public class SettDatoSendtRequestMapper {

	public SettDatoSendtRequestTo map(SettDatoSendtRequest request) {
		if (request.getDatoSendt() == null) {
			throw new ApplicationException("datoSendt has not been provided");
		}
		if (request.getJournalpostIdListe() == null) {
			throw new ApplicationException("journalpostIdListe has not been provided");
		}
		if (request.getJournalpostIdListe().isEmpty()) {
			throw new ApplicationException("journalpostIdListe is empty");
		}

		if (request.getJournalpostIdListe().isEmpty() || request.getJournalpostIdListe().contains(null)) {
			throw new ApplicationException("journalpostIdListe has an element with a null value.");
		}
		return new SettDatoSendtRequestTo(
				Collections.unmodifiableList(request.getJournalpostIdListe()),
				request.getEndretAvNavn(),
				request.getDatoSendt().toGregorianCalendar().getTime()
		);
	}
}
