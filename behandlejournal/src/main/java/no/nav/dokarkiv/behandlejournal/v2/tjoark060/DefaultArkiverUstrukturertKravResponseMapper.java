package no.nav.dokarkiv.behandlejournal.v2.tjoark060;

import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.ArkiverUstrukturertKravResponse;

/**
 * Implementation of {@link ArkiverUstrukturertKravResponseMapper}. Maps response from domain to FIM.
 * 
 * @author Rune Romundstad, Visma Consulting
 *
 */
public class DefaultArkiverUstrukturertKravResponseMapper implements ArkiverUstrukturertKravResponseMapper {

//	private Mapper dozerMapper;
	
	/** {@inheritDoc} */
	@Override
	public ArkiverUstrukturertKravResponse map(
			no.nav.dokarkiv.behandlejournal.v2.tjoark060.ArkiverUstrukturertKravResponse domainResponse) {
//		return dozerMapper.map(domainResponse, ArkiverUstrukturertKravResponse.class); FIXME
		return null;
	}
	
//	@Inject
//	@Named("dozerMapper")
//	public void setDozerMapper(Mapper dozerMapper) {
//		this.dozerMapper = dozerMapper;
//	}

}
