package no.nav.dokarkiv.fysiskslettdokument.rjoark102;

import static org.apache.commons.lang3.BooleanUtils.isFalse;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.service.BegrensningService;
import no.nav.dokarkiv.core.exceptions.BegrensningIkkeFunnetException;
import no.nav.dokarkiv.core.repository.JoarkDeleteRepository;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import no.nav.dokarkiv.fysiskslettdokument.exceptions.UgyldigTilknyttetJournalpostSomException;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class FysiskSlettDokumentService {

	private final JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;
	private final JoarkDeleteRepository deleteRepository;
	private final FysiskSlettDokumentValidator validator;
	private final BegrensningService begrensningService;

	@Inject
	public FysiskSlettDokumentService(
			JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository,
			JoarkDeleteRepository deleteRepository,
			FysiskSlettDokumentValidator validator,
			BegrensningService begrensningService) {
		this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
		this.deleteRepository = deleteRepository;
		this.validator = validator;
		this.begrensningService = begrensningService;
	}

	public FysiskSlettDokumentResponse sletteDokumentFysisk(FysiskSlettDokumentRequestTo requestTo) {
		List<JournalpostDokumentInfoRelasjon> listFoundByJournalpostdAndDokumentInfoId =
				journalpostDokumentInfoRelasjonRepository.findAllByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(
						requestTo.getJournalpostId(), requestTo.getDokumentInfoId()).orElse(new ArrayList<>());

		validator.validerAtRequestReferererTilGyldigJournalpostDokumentInfoRelasjon(listFoundByJournalpostdAndDokumentInfoId, requestTo);

		JournalpostDokumentInfoRelasjon relasjonSomSkalSlettes = listFoundByJournalpostdAndDokumentInfoId.get(0);

		switch (relasjonSomSkalSlettes.getTilknyttetJournalpostSom()) {
			case HOVEDDOKUMENT:
				sjekkAtJournalpostErUtilgjengeliggjort(relasjonSomSkalSlettes.getJournalpost().getJournalpostId());
				fysiskSlettEtHoveddokument(relasjonSomSkalSlettes);
				break;
			case VEDLEGG:
				sjekkAtDokumentErUtilgjengeliggjort(
						relasjonSomSkalSlettes.getJournalpost().getJournalpostId(),
						relasjonSomSkalSlettes.getDokumentInfo().getDokumentInfoId());
				fysiskSlettEtVedlegg(relasjonSomSkalSlettes);
				break;
			default:
				throw new UgyldigTilknyttetJournalpostSomException(String.format(
						"Kan ikke fysisk slette dokument med journalpostId=%s, dokumentInfoId=%s fordi " +
								"dokumentet er ikke tilknyttet journalposten som hoveddokument eller vedlegg.",
						requestTo.getJournalpostId(),
						requestTo.getDokumentInfoId()));
		}

		//TODO: Avklare informasjon i response
		return FysiskSlettDokumentResponse.builder()
				.journalpostId(requestTo.getJournalpostId())
				.dokumentInfoId(requestTo.getDokumentInfoId())
				.build();
	}

	private void sjekkAtJournalpostErUtilgjengeliggjort(Long journalpostId) {
		if (isFalse(begrensningService.isJournalpostBegrenset(
				journalpostId,
				BegrensningTypeCode.UTILGJENGELIGGJORT))) {
			throw new BegrensningIkkeFunnetException(String.format(
					"Fant ikke forventet begrensning for journalpost med journalpostId=%s og begrensningsType=%s.",
					journalpostId,
					BegrensningTypeCode.UTILGJENGELIGGJORT.name()));
		}
	}

	private void sjekkAtDokumentErUtilgjengeliggjort(Long journalpostId, Long dokumentInfoId) {
		if (isFalse(begrensningService.isJournalpostDokumentInfoRelasjonBegrenset(
				journalpostId,
				dokumentInfoId,
				BegrensningTypeCode.UTILGJENGELIGGJORT))) {
			throw new BegrensningIkkeFunnetException(String.format(
					"Fant ikke forventet begrensning for dokument med journalpostId=%s, dokumentInfoId=%s og begrensningsType=%s.",
					journalpostId,
					dokumentInfoId,
					BegrensningTypeCode.UTILGJENGELIGGJORT.name()));
		}
	}

	private void fysiskSlettEtHoveddokument(JournalpostDokumentInfoRelasjon relasjonSomSkalSlettes) {
	}

	private void fysiskSlettEtVedlegg(JournalpostDokumentInfoRelasjon relasjonSomSkalSlettes) {
	}


	// SLETTELINJE ----------------------------------------------------------

	private void fysiskSlettEtVedleggKnyttetEnJP(FysiskSlettDokumentRequestTo requestTo) {
		List<JournalpostDokumentInfoRelasjon> listFoundByDokumentInfoId =
				journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(requestTo.getDokumentInfoId())
						.orElse(new ArrayList<>());

//		validator.validerFysiskSlettEtVedleggKnyttetEnJP(listFoundByDokumentInfoId, requestTo);

		JournalpostDokumentInfoRelasjon vedleggRelasjonSomSkalSlettes = listFoundByDokumentInfoId.get(0);

		slettEtDokumentMedAllMetadata(vedleggRelasjonSomSkalSlettes.getDokumentInfo().getDokumentInfoId());
		log.info("{} har utført fysisk sletting av vedlegg med journalpostId={}, dokumentInfoId={}",
				MDC.get(MDCConstants.MDC_REQUEST_ID), requestTo.getJournalpostId(), requestTo.getDokumentInfoId());
	}


	private void fysiskSlettEtHoveddokumentKnyttetEnJP(FysiskSlettDokumentRequestTo requestTo) {
		List<JournalpostDokumentInfoRelasjon> listFoundByDokumnentInfoId =
				journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(requestTo.getDokumentInfoId())
						.orElse(new ArrayList<>());

//		validator.validerFysiskSlettEtHoveddokumentKnyttetEnJP(listFoundByDokumnentInfoId, requestTo);

		JournalpostDokumentInfoRelasjon hoveddokumentRelasjon = listFoundByDokumnentInfoId.get(0);

		slettEventuelleVedleggKnyttetHoveddokumentValidertForSletting(hoveddokumentRelasjon);

		slettEnJournalpostOgEtDokumentMedAllMetadata(hoveddokumentRelasjon);
		log.info("{} har utført fysisk sletting av hoveddokument med journalpostId={}, dokumentInfoId={}",
				MDC.get(MDCConstants.MDC_REQUEST_ID), requestTo.getJournalpostId(), requestTo.getDokumentInfoId());
	}

	private void slettEventuelleVedleggKnyttetHoveddokumentValidertForSletting(JournalpostDokumentInfoRelasjon hoveddokumentRelasjon) {
		List<JournalpostDokumentInfoRelasjon> listFoundByJournalpostId =
				journalpostDokumentInfoRelasjonRepository.findAllByJournalpostJournalpostId(hoveddokumentRelasjon.getJournalpost()
						.getJournalpostId())
						.orElse(new ArrayList<>());

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
				.begrensningType(BegrensningTypeCode.UTILGJENGELIGGJORT)
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
