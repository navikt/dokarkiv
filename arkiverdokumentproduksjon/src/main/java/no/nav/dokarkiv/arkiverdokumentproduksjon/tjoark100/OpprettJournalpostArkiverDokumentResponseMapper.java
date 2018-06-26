package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark100;

import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostArkiverDokumentResponse;
import org.springframework.stereotype.Component;

/**
 * Implementation of OpprettJournalpostAkiverDokumentResponseMapper
 *
 * @author Torgeir Cook
 */
@Component
public class OpprettJournalpostArkiverDokumentResponseMapper {

	public OpprettJournalpostArkiverDokumentResponse map(OpprettJournalpostArkiverDokumentResponseTo domainResponse) {
		return new OpprettJournalpostArkiverDokumentResponse()
				.withDokumentInfoId(domainResponse.getDokumentInfoId())
				.withJournalpostId(domainResponse.getJournalpostId());
	}

}
