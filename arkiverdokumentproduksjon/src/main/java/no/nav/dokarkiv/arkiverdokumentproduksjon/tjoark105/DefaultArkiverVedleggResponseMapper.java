package no.nav.provider.dok.joark.nsb.map.support;

import no.nav.provider.dok.joark.nsb.map.ArkiverVedleggResponseMapper;
import no.nav.service.dok.joark.nsb.to.ArkiverVedleggResponseTo;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.ArkiverVedleggResponse;
import org.dozer.Mapper;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Magnar Brandsdal, Visma Consulting
 */
public class DefaultArkiverVedleggResponseMapper implements ArkiverVedleggResponseMapper {

	private Mapper dozerMapper;

	@Override
	public ArkiverVedleggResponse map(ArkiverVedleggResponseTo arkiverVedleggResponseTo) {
		return dozerMapper.map(arkiverVedleggResponseTo, ArkiverVedleggResponse.class);
	}

	@Inject
	@Named("dozerMapper")
	public void setDozerMapper(Mapper dozerMapper) {
		this.dozerMapper = dozerMapper;
	}

}
