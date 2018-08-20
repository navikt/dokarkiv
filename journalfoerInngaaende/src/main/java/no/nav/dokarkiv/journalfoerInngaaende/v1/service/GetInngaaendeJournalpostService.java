package no.nav.dokarkiv.journalfoerInngaaende.v1.service;

import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.Utils.convertStringToLong;

import no.nav.dok.tjenester.journalfoerinngaaende.GetJournalpostResponse;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.DokarkivRestFunctionalException;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.journalfoerInngaaende.v1.map.GetInngaaendeJournalpostMapper;
import org.springframework.http.HttpStatus;
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

	public GetJournalpostResponse getInngaaendeJournalpostByJournalpostId(String journalpostIdString) throws DokarkivRestFunctionalException {
		Long journalpostId = convertStringToLong(journalpostIdString, "journalpostId");

		Journalpost journalpost = joarkRepository.findById(journalpostId)
				.orElseThrow(() -> new DokarkivRestFunctionalException(String.format("Kunne ikke finne journalpost med journalpostId=%s i Joark", journalpostId), HttpStatus.NOT_FOUND));

		if (!journalpost.isInngaende()) {
			throw new DokarkivRestFunctionalException("Journalpost er ikke av type Inngaaende", HttpStatus.BAD_REQUEST);
		}

		try {
			return getInngaaendeJournalpostMapper.map(journalpost);
		} catch (Exception e) {
			throw new DokarkivRestFunctionalException(String.format("Kunne ikke mappe Journalpost. Feilmelding: %s",
					journalpost.getJournalpostId(), e.getMessage()), HttpStatus.BAD_REQUEST);
		}
	}
}