package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark105;

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
