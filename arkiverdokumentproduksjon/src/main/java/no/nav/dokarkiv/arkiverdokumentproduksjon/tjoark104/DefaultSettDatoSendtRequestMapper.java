package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark104;

import com.google.common.collect.ImmutableList;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.service.dok.joark.nsb.to.SettDatoSendtRequestTo;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.SettDatoSendtRequest;

/**
 * Default implementation of SettDatoSendtRequestMapper
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class DefaultSettDatoSendtRequestMapper implements SettDatoSendtRequestMapper {

	@Override
	public SettDatoSendtRequestTo map(SettDatoSendtRequest request) {
		if(request.getDatoSendt() == null) {
			throw new ApplicationException("datoSendt has not been provided");
		}
		if(request.getJournalpostIdListe() == null) {
			throw new ApplicationException("journalpostIdListe has not been provided");
		}
		if(request.getJournalpostIdListe().isEmpty()) {
			throw new ApplicationException("journalpostIdListe is empty");
		}

		try {
			return new SettDatoSendtRequestTo(
					ImmutableList.copyOf(request.getJournalpostIdListe()),
					request.getEndretAvNavn(),
					request.getDatoSendt().toGregorianCalendar().getTime()
			);
		} catch(NullPointerException e) {
			throw new ApplicationException("journalpostIdListe has an element with a null value " + e.getMessage() , e);
		}
	}
}
