package no.nav.dokarkiv.fysiskslettdokument.rjoark102;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.repository.JoarkDeleteRepository;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class FysiskSlettDokumentService {

	@Inject
	private JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;

	@Inject
	private JoarkDeleteRepository deleteRepository;

	@Inject
	private FysiskSlettDokumentValidator validator;


	//IKKE FERDIG, LOGIKK ER IKKE BESTEMT.
	public FysiskSlettDokumentResponse slettDokumentFysisk(FysiskSlettDokumentRequestTo requestTo) {
		List<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjonList = journalpostDokumentInfoRelasjonRepository.findByDokumentInfoId(requestTo
				.getDokumentInfoId()).orElse(new ArrayList<>());

		validator.validateFysiskSlettDokument(journalpostDokumentInfoRelasjonList, requestTo);

		//switch for å bestemme hvilken type av sletting. Styrt av hjemmel???
//		fysiskSlettAvDokumenter(journalpostDokumentInfoRelasjonList);

		return FysiskSlettDokumentResponseMapper.mapToFysiskSlettDokumentResponse(journalpostDokumentInfoRelasjonList.get(0)
						.getJournalpost(),
				journalpostDokumentInfoRelasjonList.get(0).getDokumentInfo());
	}

	private void fysiskSlettAvDokumenter(List<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjonList) {
		//her skal vi slette alle dokumenter i listen
	}


	private void slettJournalpost(Long journalpostId) {
		deleteRepository.deleteJPTilleggByJournalpostId(journalpostId);
		deleteRepository.deleteSaksrelasjonByJournalpostId(journalpostId);
		deleteRepository.deleteJournalpostByJournalpostId(journalpostId);
	}

	private void slettFilOgDokumentInfo(Long dokumentInfoId) {
		slettFilBeholdDokumentInfo(dokumentInfoId);
		deleteRepository.deleteDokInfoTilleggByDokumentInfoId(dokumentInfoId);
		deleteRepository.deleteDokInfoJPRelByDokumentInfoId(dokumentInfoId);
		deleteRepository.deleteDokInfoByDokumentInfoId(dokumentInfoId);
	}

	private void slettFilBeholdDokumentInfo(Long dokumentInfoId) {
		deleteRepository.deleteDokumentFilByDokumentInfoId(dokumentInfoId);
		deleteRepository.deleteFilDetaljerByDokumentInfoId(dokumentInfoId);
	}
}
