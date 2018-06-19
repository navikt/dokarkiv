package no.nav.dokarkiv.dokumentproduksjoninfo.tjoark122;

import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentJournalpostInfoRequest;
import org.springframework.stereotype.Component;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class HentJournalpostInfoRequestMapper {
	public HentJournalpostInfoRequestTo map(HentJournalpostInfoRequest request) {
		return HentJournalpostInfoRequestTo.builder()
				.journalpostId(request.getJournalpostId())
				.dokumentInfoId(request.getDokumentInfoId())
				.build();
	}
}
