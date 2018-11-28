package no.nav.dokarkiv.journalfoerinngaaende.v1.rjoark001i;

import no.nav.dok.tjenester.journalfoerinngaaende.GetJournalpostResponse;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.repository.JoarkRepositoryBegrenset;
import no.nav.dokarkiv.journalfoerinngaaende.v1.util.Utils;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Service
public class GetInngaaendeJournalpostService {

    private final JoarkRepositoryBegrenset joarkRepository;
	private final GetInngaaendeJournalpostMapper getInngaaendeJournalpostMapper;

	@Inject
    public GetInngaaendeJournalpostService(JoarkRepositoryBegrenset joarkRepository,
                                           GetInngaaendeJournalpostMapper getInngaaendeJournalpostMapper) {
		this.joarkRepository = joarkRepository;
		this.getInngaaendeJournalpostMapper = getInngaaendeJournalpostMapper;
	}

	public GetJournalpostResponse getInngaaendeJournalpostByJournalpostId(String journalpostIdString) {
		Long journalpostId = Utils.convertStringToLong(journalpostIdString, "journalpostId");

		Journalpost journalpost = joarkRepository.findById(journalpostId)
				.orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", journalpostIdString)));

		Utils.assertJournalpostIsInngaaende(journalpost);

		Utils.filterFildetaljer(journalpost);

		return getInngaaendeJournalpostMapper.map(journalpost);
	}
}