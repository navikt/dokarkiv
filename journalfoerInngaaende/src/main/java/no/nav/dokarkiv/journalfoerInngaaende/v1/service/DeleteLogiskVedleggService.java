package no.nav.dokarkiv.journalfoerInngaaende.v1.service;

import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.Utils.convertStringToLong;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.DokarkivRestFunctionalException;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.repository.SkannetInnholdRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Service
public class DeleteLogiskVedleggService {

	private JoarkRepository joarkRepository;
	private SkannetInnholdRepository skannetInnholdRepository;

	@Inject
	public DeleteLogiskVedleggService(JoarkRepository joarkRepository,
									  SkannetInnholdRepository skannetInnholdRepository) {
		this.joarkRepository = joarkRepository;
		this.skannetInnholdRepository = skannetInnholdRepository;
	}

	//TODO: Sporingsinfo
	public void delete(String journalpostIdString, String dokumentIdString, String logiskVedleggIdString) throws DokarkivRestFunctionalException {
		Long journalpostId = convertStringToLong(journalpostIdString, "journalpostId");

		Journalpost journalpost = joarkRepository.findById(journalpostId)
				.orElseThrow(() -> new DokarkivRestFunctionalException("Kunne ikke finne journalpost i Joark", HttpStatus.NOT_FOUND));

		if (!journalpost.isInngaende()) {
			throw new DokarkivRestFunctionalException("Journalpost er ikke av type Inngaaende", HttpStatus.BAD_REQUEST); //TODO Annen HttpStatus?
		}

		skannetInnholdRepository.findSkannetInnholdBySkannetInnholdIdAndDokumentinfoId(logiskVedleggIdString, dokumentIdString)
				.orElseThrow(() -> new DokarkivRestFunctionalException(String.format("Kunne ikke finne logisk vedlegg med logiskVedleggId=%s og dokumentId=%s i Joark", logiskVedleggIdString, dokumentIdString),
						HttpStatus.NOT_FOUND));

		skannetInnholdRepository.deleteSkannetInnholdBySkannetInnholdIdAndDokumentinfoId(logiskVedleggIdString, dokumentIdString);
	}
}