package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark108;


import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.FerdigstillJournalpostRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * Implementation of {@link FerdigstillJournalpostRequestMapper} interface
 *
 * @author Stig Strøm
 */
@Component
public class FerdigstillJournalpostRequestMapper {

	public FerdigstillJournalpostRequestTo map(FerdigstillJournalpostRequest wsRequest) {
		Assert.notNull(wsRequest, "Request is null");
		UtsendingsKanalCode utsendingskanal = null;
		if (wsRequest.getUtsendingskanal() != null) {
			utsendingskanal = UtsendingsKanalCode.valueOf(wsRequest.getUtsendingskanal());
		}
		return new FerdigstillJournalpostRequestTo(wsRequest.getJournalpostId(), wsRequest.getEndretAvNavn(), utsendingskanal);
	}

}
