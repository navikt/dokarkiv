package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark112;

import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostArkiverDokumenterResponse;
import org.springframework.stereotype.Component;

/**
 * @author Torgeir Cook
 */
@Component
public class OpprettJournalpostArkiverDokumenterResponseMapper {

	public OpprettJournalpostArkiverDokumenterResponse map(OpprettJournalpostArkiverDokumenterResponseTo domainResponse) {
		return new OpprettJournalpostArkiverDokumenterResponse()
				.withJournalpostId(domainResponse.getJournalpostId())
				.withDokumentInfoIdListe(domainResponse.getDokumentInfoIdList());
	}

}
