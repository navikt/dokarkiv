package no.nav.dokarkiv.oppdatermetadata.v1.temp001;

import static no.nav.dokarkiv.oppdatermetadata.v1.support.OppdaterMetadataValidator.validateOppdaterteFelt;
import static no.nav.dokarkiv.oppdatermetadata.v1.util.Utils.convertStringToLong;

import no.nav.dok.oppdatermetadata.api.v1.PutOppdatermetadataRequest;
import no.nav.dok.oppdatermetadata.api.v1.PutOppdatermetadataResponse;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Paul Magne Lunde, Visma Consulting
 */
@Service
public class OppdaterMetadataService {

    private final JoarkRepositorySkjermet joarkRepository;
	private final PutJournalpostMapper putJournalpostMapper;

	@Inject
    public OppdaterMetadataService(JoarkRepositorySkjermet joarkRepository,
								   PutJournalpostMapper putJournalpostMapper) {
		this.joarkRepository = joarkRepository;
		this.putJournalpostMapper = putJournalpostMapper;
	}

	public PutOppdatermetadataResponse updateInngaaendeJournalpost(String journalpostId, PutOppdatermetadataRequest putOppdatermetadataRequest) throws InputValideringFeiletException {
		Journalpost journalpost = joarkRepository.findById(convertStringToLong(journalpostId, "journalpostId"))
				.orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", journalpostId)));

		validateOppdaterteFelt(putOppdatermetadataRequest, journalpost.getJournalstatus());
		putJournalpostMapper.oppdaterJournalpost(journalpost, putOppdatermetadataRequest);
		joarkRepository.save(journalpost);
		return PutOppdatermetadataResponse.builder()
				.journalpostId(journalpostId)
				.build();
	}
}
