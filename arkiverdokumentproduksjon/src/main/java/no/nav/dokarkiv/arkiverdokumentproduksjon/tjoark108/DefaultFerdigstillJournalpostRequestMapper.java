package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark108;


import no.nav.domain.dok.joark.codestable.UtsendingsKanalCode;
import no.nav.service.dok.joark.nsb.to.FerdigstillJournalpostRequestTo;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.FerdigstillJournalpostRequest;
import org.springframework.util.Assert;

/**
 * Implementation of {@link FerdigstillJournalpostRequestMapper} interface
 * 
 * @author Stig Strøm
 *
 */
public class DefaultFerdigstillJournalpostRequestMapper implements FerdigstillJournalpostRequestMapper {

	@Override
	public FerdigstillJournalpostRequestTo map(FerdigstillJournalpostRequest wsRequest) {
		Assert.notNull(wsRequest, "Request is null");
		UtsendingsKanalCode utsendingskanal = null;
		if (wsRequest.getUtsendingskanal() != null) {
			utsendingskanal = UtsendingsKanalCode.valueOf(wsRequest.getUtsendingskanal());
		}
		return new FerdigstillJournalpostRequestTo(wsRequest.getJournalpostId(), wsRequest.getEndretAvNavn(), utsendingskanal);
	}

}
