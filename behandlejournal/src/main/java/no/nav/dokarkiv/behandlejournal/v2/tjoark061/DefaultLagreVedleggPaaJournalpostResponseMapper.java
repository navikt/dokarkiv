package no.nav.dokarkiv.behandlejournal.v2.tjoark061;

import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.LagreVedleggPaaJournalpostResponse;
import org.springframework.stereotype.Component;

/**
 * Implementation of {@link LagreVedleggPaaJournalpostResponseMapper}
 * 
 * @author Rune Romundstad, Visma Consulting
 *
 */
@Component
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
