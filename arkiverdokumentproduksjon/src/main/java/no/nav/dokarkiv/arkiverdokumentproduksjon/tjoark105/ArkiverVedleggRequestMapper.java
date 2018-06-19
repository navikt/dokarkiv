package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark105;

import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.ArkiverVedleggRequest;

/**
 * Mapper for ArkiverVedleggRequest, ws to domain
 *
 * @author Magnar Brandsdal, Visma Consulting
 */
public interface ArkiverVedleggRequestMapper {

	ArkiverVedleggRequestTo map(ArkiverVedleggRequest arkiverVedleggRequest);

}
