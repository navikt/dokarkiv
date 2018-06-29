package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark109;

import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.KnyttDokumentTilJournalpostSomVedleggRequest;
import org.springframework.stereotype.Component;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Component
public class KnyttDokumentTilJournalpostSomVedleggRequestMapper {

	public KnyttDokumentTilJournalpostSomVedleggRequestTo map(KnyttDokumentTilJournalpostSomVedleggRequest request) {
		return KnyttDokumentTilJournalpostSomVedleggRequestTo.builder()
				.dokumentInfoId(request.getDokumentInfoId())
				.endretAvNavn(request.getEndretAvNavn())
				.knyttesFraJournalpostId(request.getKnyttesFraJournalpostId())
				.knyttesTilJournalpostId(request.getKnyttesTilJournalpostId())
				.build();
	}

}
