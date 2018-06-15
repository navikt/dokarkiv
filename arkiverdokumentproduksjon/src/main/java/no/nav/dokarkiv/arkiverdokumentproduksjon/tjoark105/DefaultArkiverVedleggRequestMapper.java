package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark105;

import no.nav.service.dok.joark.nsb.to.ArkiverVedleggRequestTo;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.ArkiverVedleggRequest;
import org.dozer.Mapper;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Implementation of ArkiverVedleggRequestMapper
 *
 * @author Magnar Brandsdal, Visma Consulting
 */
public class DefaultArkiverVedleggRequestMapper implements ArkiverVedleggRequestMapper {

	private Mapper dozerMapper;

	@Override
	public ArkiverVedleggRequestTo map(ArkiverVedleggRequest arkiverVedleggRequest) {
		return dozerMapper.map(arkiverVedleggRequest, ArkiverVedleggRequestTo.class);
	}

	@Inject
	@Named("dozerMapper")
	public void setDozerMapper(Mapper dozerMapper) {
		this.dozerMapper = dozerMapper;
	}
}
