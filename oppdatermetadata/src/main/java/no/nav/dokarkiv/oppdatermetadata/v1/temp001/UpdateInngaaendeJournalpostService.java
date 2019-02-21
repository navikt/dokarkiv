package no.nav.dokarkiv.oppdatermetadata.v1.temp001;

import static no.nav.dokarkiv.oppdatermetadata.v1.support.JournalpostValidator.validateJournalpostStatuser;
import static no.nav.dokarkiv.oppdatermetadata.v1.util.Utils.convertStringToLong;

import no.nav.dok.oppdatermetadata.api.v1.PutOppdatermetadataRequest;
import no.nav.dok.oppdatermetadata.api.v1.PutOppdatermetadataResponse;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Paul Magne Lunde, Visma Consulting
 */
@Service
public class UpdateInngaaendeJournalpostService {

    private final JoarkRepositorySkjermet joarkRepository;
	private final PutInngaaendeJournalpostMapper putInngaaendeJournalpostMapper;

	@Inject
    public UpdateInngaaendeJournalpostService(JoarkRepositorySkjermet joarkRepository,
											  PutInngaaendeJournalpostMapper putInngaaendeJournalpostMapper) {
		this.joarkRepository = joarkRepository;
		this.putInngaaendeJournalpostMapper = putInngaaendeJournalpostMapper;
	}

	public PutOppdatermetadataResponse updateInngaaendeJournalpost(String journalpostId, PutOppdatermetadataRequest putOppdatermetadataRequest) {
		Journalpost journalpost = joarkRepository.findById(convertStringToLong(journalpostId, "journalpostId"))
				.orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", journalpostId)));

		validateJournalpostStatuser(journalpost);
		putInngaaendeJournalpostMapper.oppdaterJournalpost(journalpost, putOppdatermetadataRequest);
		joarkRepository.save(journalpost);
		return PutOppdatermetadataResponse.builder()
				.journalpostId(journalpostId)
				.build();
	}
}
