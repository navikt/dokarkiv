package no.nav.dokarkiv.behandlejournal.v2.tjoark061;

import org.springframework.stereotype.Component;

/**
 * Implementation of {@link DefaultLagreVedleggPaaJournalpostRequestMapper}
 * 
 * @author Rune Romundstad, Visma Consulting
 */
@Component
public class DefaultLagreVedleggPaaJournalpostRequestMapper implements LagreVedleggPaaJournalpostRequestMapper {

//	private Mapper dozerMapper;
	
	/**{@inheritDoc} */
	@Override
	public LagreVedleggPaaJournalpostRequest map(
			no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.LagreVedleggPaaJournalpostRequest wsRequest) {
//		return dozerMapper.map(wsRequest, LagreVedleggPaaJournalpostRequest.class);
		return null;
	}
	
//	@Inject
//	@Named("dozerMapper")
//	public void setDozerMapper(Mapper dozerMapper) {
//		this.dozerMapper = dozerMapper;
//	}

}
