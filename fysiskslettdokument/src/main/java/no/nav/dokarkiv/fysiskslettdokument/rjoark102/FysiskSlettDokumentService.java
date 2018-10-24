package no.nav.dokarkiv.fysiskslettdokument.rjoark102;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.repository.JoarkDeleteRepository;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import no.nav.dokarkiv.fysiskslettdokument.exceptions.UgyldigHjemmelException;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Slf4j
@Service
public class FysiskSlettDokumentService {

	private final JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;
	private final JoarkDeleteRepository deleteRepository;
	private final FysiskSlettDokumentValidator validator;

	@Inject
	public FysiskSlettDokumentService(
			JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository,
			JoarkDeleteRepository deleteRepository,
			FysiskSlettDokumentValidator validator) {
		this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
		this.deleteRepository = deleteRepository;
		this.validator = validator;
	}

	public FysiskSlettDokumentResponse slettDokumentFysisk(FysiskSlettDokumentRequestTo requestTo)
			throws UgyldigHjemmelException {
		String hjemmelSomStyrerSletteMetode = requestTo.getHjemmel();

		//TODO: Erstatt med HjemmelCode når det er avklart
		switch (hjemmelSomStyrerSletteMetode) {
			case "slettKunEttVedleggFraForsendeleKnyttetJP":
				slettKunEttVedleggFraForsendelseKnyttetJP(requestTo);
				break;
			default:
				throw new UgyldigHjemmelException(
						String.format("%s kan ikke slette dokument pga. ugyldig hjemmel. hjemmel=%s, dokumentInfoId=%s, journalpostId=%s",
								MDC.get(MDCConstants.MDC_REQUEST_ID),
								hjemmelSomStyrerSletteMetode,
								requestTo.getDokumentInfoId(),
								requestTo.getJournalpostId()));
		}

		//TODO: Avklare informasjon i response
		return FysiskSlettDokumentResponse.builder()
				.journalpostId(requestTo.getJournalpostId())
				.dokumentInfoId(requestTo.getDokumentInfoId())
				.build();
	}

	private void slettKunEttVedleggFraForsendelseKnyttetJP(FysiskSlettDokumentRequestTo requestTo) {
		JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon =
				journalpostDokumentInfoRelasjonRepository.findByJournalpostId(requestTo.getJournalpostId());

		validator.validerAtKunEtVedleggSkalSlettes(journalpostDokumentInfoRelasjon, requestTo);

		slettEtDokumentMedAlleMetadata(requestTo.getDokumentInfoId());
	}

	private void slettEtDokumentMedAlleMetadata(Long dokumentInfoId) {
		slettFilOgDokumentInfo(dokumentInfoId);
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
