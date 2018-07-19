package no.nav.dokarkiv.innsynjournal.v2.tjoark053;

import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.HentTilgjengeligJournalpostListeRequest;
import org.springframework.stereotype.Component;

/**
 * Mapping from {@link HentTilgjengeligJournalpostListeRequest} to {@link HentJournalpostListeToRequest}
 *
 * @author Torgeir Cook, Visma Consulting.
 *
 */
@Component
public class HentMinTilgjengeligeJournalpostListeV2RequestMapper {

//	private Mapper dozerMapper;

	public HentJournalpostListeToRequest map(HentTilgjengeligJournalpostListeRequest request) {
//		return dozerMapper.map(request, HentJournalpostListeToRequest.class);
		return null;
	}

//	@Inject
//	@Named("dozerMapper")
//	public void setDozerMapper(Mapper dozerMapper) {
//		this.dozerMapper = dozerMapper;
//	}
}
