package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark105;

import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.ArkiverVedleggResponse;
import org.springframework.stereotype.Component;

/**
 * @author Magnar Brandsdal, Visma Consulting
 */
@Component
public class ArkiverVedleggResponseMapper {

	public ArkiverVedleggResponse map(ArkiverVedleggResponseTo arkiverVedleggResponseTo) {
		return new ArkiverVedleggResponse()
				.withDokumentInfoId(arkiverVedleggResponseTo.getDokumentInfoId())
				.withJournalpostId(arkiverVedleggResponseTo.getJournalpostId());
	}
}
