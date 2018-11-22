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
import java.util.List;

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

	public FysiskSlettDokumentResponse slettDokumentFysisk(FysiskSlettDokumentRequestTo requestTo) {
		String hjemmelSomStyrerSletteMetode = requestTo.getHjemmel();

		//TODO: Erstatt med HjemmelCode når det er avklart
		switch (hjemmelSomStyrerSletteMetode) {
			case "fysiskSlettEtVedleggKnyttetEnJP":
				fysiskSlettEtVedleggKnyttetEnJP(requestTo);
				break;
			case "fysiskSlettEtHoveddokumentKnyttetEnJP":
				fysiskSlettEtHoveddokumentKnyttetEnJP(requestTo);
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

	private void fysiskSlettEtVedleggKnyttetEnJP(FysiskSlettDokumentRequestTo requestTo) {
		List<JournalpostDokumentInfoRelasjon> listFoundByDokumentInfoId =
				journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(requestTo.getDokumentInfoId());

		validator.validerFysiskSlettEtVedleggKnyttetEnJP(listFoundByDokumentInfoId, requestTo);

		JournalpostDokumentInfoRelasjon vedleggRelasjonSomSkalSlettes = listFoundByDokumentInfoId.get(0);

		slettEtDokumentMedAllMetadata(vedleggRelasjonSomSkalSlettes.getDokumentInfo().getDokumentInfoId());
		log.info("{} har utført fysisk sletting av vedlegg med journalpostId={}, dokumentInfoId={}",
				MDC.get(MDCConstants.MDC_REQUEST_ID), requestTo.getJournalpostId(), requestTo.getDokumentInfoId());
	}


	private void fysiskSlettEtHoveddokumentKnyttetEnJP(FysiskSlettDokumentRequestTo requestTo) {
		List<JournalpostDokumentInfoRelasjon> listFoundByDokumnentInfoId =
				journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(requestTo.getDokumentInfoId());

		validator.validerFysiskSlettEtHoveddokumentKnyttetEnJP(listFoundByDokumnentInfoId, requestTo);

		JournalpostDokumentInfoRelasjon hoveddokumentRelasjon = listFoundByDokumnentInfoId.get(0);

		slettEventuelleVedleggKnyttetHoveddokumentValidertForSletting(hoveddokumentRelasjon);

		slettEnJournalpostOgEtDokumentMedAllMetadata(hoveddokumentRelasjon);
		log.info("{} har utført fysisk sletting av hoveddokument med journalpostId={}, dokumentInfoId={}",
				MDC.get(MDCConstants.MDC_REQUEST_ID), requestTo.getJournalpostId(), requestTo.getDokumentInfoId());
	}

	private void slettEventuelleVedleggKnyttetHoveddokumentValidertForSletting(JournalpostDokumentInfoRelasjon hoveddokumentRelasjon) {
		List<JournalpostDokumentInfoRelasjon> listFoundByJournalpostId =
				journalpostDokumentInfoRelasjonRepository.findAllByJournalpostJournalpostId(hoveddokumentRelasjon.getJournalpost()
						.getJournalpostId());

		for (JournalpostDokumentInfoRelasjon relasjon : listFoundByJournalpostId) {
			if (relasjon.isVedlegg()) {
				opprettRequestTilFysiskSlettEtVedleggKnyttetEnJPFraEnVedleggsRelasjon(relasjon);
			}
		}
	}

	private void opprettRequestTilFysiskSlettEtVedleggKnyttetEnJPFraEnVedleggsRelasjon(JournalpostDokumentInfoRelasjon vedleggRelasjon) {
		FysiskSlettDokumentRequestTo requestTo = FysiskSlettDokumentRequestTo.builder()
				.journalpostId(vedleggRelasjon.getJournalpost().getJournalpostId())
				.dokumentInfoId(vedleggRelasjon.getDokumentInfo().getDokumentInfoId())
				.hjemmel("fysiskSlettEtVedleggKnyttetEnJP")
				.build();
		fysiskSlettEtVedleggKnyttetEnJP(requestTo);
	}

	private void slettEtDokumentMedAllMetadata(Long dokumentInfoId) {
		slettFilOgDokumentInfo(dokumentInfoId);
	}

	private void slettEnJournalpostOgEtDokumentMedAllMetadata(JournalpostDokumentInfoRelasjon relasjon) {
		slettFilOgDokumentInfo(relasjon.getDokumentInfo().getDokumentInfoId());
		slettJournalpost(relasjon.getJournalpost().getJournalpostId());
	}

	private void slettJournalpost(Long journalpostId) {
		deleteRepository.deleteJPTilleggByJournalpostId(journalpostId);
		deleteRepository.deleteSaksrelasjonByJournalpostId(journalpostId);
		deleteRepository.deleteBrukerByJournalpostId(journalpostId);
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
