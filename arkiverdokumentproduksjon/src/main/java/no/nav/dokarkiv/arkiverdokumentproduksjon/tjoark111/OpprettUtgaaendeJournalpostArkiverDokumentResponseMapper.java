package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.arkiverdokumentproduksjon.JournalTilstand;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettUtgaaendeJournalpostArkiverDokumentResponse;
import org.springframework.stereotype.Component;

/**
 * Implementation of OpprettJournalpostAkiverDokumentResponseMapper
 *
 * @author Torgeir Cook
 */
@Component
public class OpprettUtgaaendeJournalpostArkiverDokumentResponseMapper {

	public OpprettUtgaaendeJournalpostArkiverDokumentResponse map(OpprettUtgaaendeJournalpostArkiverDokumentResponseTo domainResponse) {
		return new OpprettUtgaaendeJournalpostArkiverDokumentResponse()
				.withDokumentInfoIdHoveddokument(domainResponse.getDokumentInfoIdHoveddokument())
				.withJournalpostId(domainResponse.getJournalpostId())
				.withDokumentInfoIdVedleggListe(domainResponse.getDokumentInfoIdVedlegg())
				.withJournalTilstand(convertJournalStatusToJournalTilstand(domainResponse.getJournalStatus()));
	}

	private JournalTilstand convertJournalStatusToJournalTilstand(JournalStatusCode journalStatus) {
		if (JournalStatusCode.FS == journalStatus || JournalStatusCode.FL == journalStatus) {
			return JournalTilstand.FERDIGSTILT;
		}
		return JournalTilstand.UNDER_ARBEID;
	}
}
