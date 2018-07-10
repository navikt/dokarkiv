package no.nav.dokarkiv.behandlejournal.v2.tjoark061;

import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.LagreVedleggPaaJournalpostResponse;

/**
 * Implementation of {@link LagreVedleggPaaJournalpostResponseMapper}
 * 
 * @author Rune Romundstad, Visma Consulting
 *
 */
public class DefaultLagreVedleggPaaJournalpostResponseMapper implements LagreVedleggPaaJournalpostResponseMapper {

//	private Mapper dozerMapper;
	
	/**{@inheritDoc} */
	@Override
	public LagreVedleggPaaJournalpostResponse map(
			no.nav.dokarkiv.behandlejournal.v2.tjoark061.LagreVedleggPaaJournalpostResponse domainResponse) {
//		return dozerMapper.map(domainResponse, LagreVedleggPaaJournalpostResponse.class);
		return null;
	}

//	@Inject
//	@Named("dozerMapper")
//	public void setDozerMapper(Mapper dozerMapper) {
//		this.dozerMapper = dozerMapper;
//	}
}
