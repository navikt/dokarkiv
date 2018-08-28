package no.nav.dokarkiv.journalfoerInngaaende.v1.service;

import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.Utils.assetJournalpostIsInngaaende;
import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.Utils.convertStringToLong;

import no.nav.dok.tjenester.journalfoerinngaaende.GetJournalpostResponse;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.journalfoerInngaaende.v1.map.GetInngaaendeJournalpostMapper;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Service
public class GetInngaaendeJournalpostService {

	private JoarkRepository joarkRepository;
	private GetInngaaendeJournalpostMapper getInngaaendeJournalpostMapper;

	@Inject
	public GetInngaaendeJournalpostService(JoarkRepository joarkRepository,
										   GetInngaaendeJournalpostMapper getInngaaendeJournalpostMapper) {
		this.joarkRepository = joarkRepository;
		this.getInngaaendeJournalpostMapper = getInngaaendeJournalpostMapper;
	}

	public GetJournalpostResponse getInngaaendeJournalpostByJournalpostId(String journalpostIdString) {
		Long journalpostId = convertStringToLong(journalpostIdString, "journalpostId");

		Journalpost journalpost = joarkRepository.findById(journalpostId)
				.orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", journalpostIdString)));

		assetJournalpostIsInngaaende(journalpost);

		return getInngaaendeJournalpostMapper.map(journalpost);
	}
}