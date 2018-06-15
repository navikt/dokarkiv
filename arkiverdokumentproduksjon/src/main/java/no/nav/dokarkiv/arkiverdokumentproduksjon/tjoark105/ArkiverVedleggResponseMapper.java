package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark105;

import no.nav.service.dok.joark.nsb.to.ArkiverVedleggResponseTo;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.ArkiverVedleggResponse;

/**
 * Mapper for ArkiverVedleggResponse, domain to ws
 *
 * @author Magnar Brandsdal, Visma Consulting
 */
public interface ArkiverVedleggResponseMapper {

	ArkiverVedleggResponse map(ArkiverVedleggResponseTo arkiverVedleggResponseTo);

}
