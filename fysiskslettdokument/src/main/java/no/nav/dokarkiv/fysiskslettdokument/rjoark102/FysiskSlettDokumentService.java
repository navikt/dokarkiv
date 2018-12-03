package no.nav.dokarkiv.fysiskslettdokument.rjoark102;

import static org.apache.commons.lang3.BooleanUtils.isFalse;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.service.BegrensningService;
import no.nav.dokarkiv.core.exceptions.BegrensningIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostDokumentInfoRelasjonIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.UgyldigTilknyttetJournalpostSomException;
import no.nav.dokarkiv.core.repository.JoarkDeleteRepository;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

@Slf4j
@Service
public class FysiskSlettDokumentService {

	private final JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;
	private final JoarkDeleteRepository deleteRepository;
	private final BegrensningService begrensningService;

	@Inject
	public FysiskSlettDokumentService(
			JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository,
			JoarkDeleteRepository deleteRepository,
			BegrensningService begrensningService) {
		this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
		this.deleteRepository = deleteRepository;
		this.begrensningService = begrensningService;
	}

	public FysiskSlettDokumentResponse sletteDokumentFysisk(FysiskSlettDokumentRequestTo requestTo) {
		JournalpostDokumentInfoRelasjon relasjonSomSkalSlettesFysisk =
				journalpostDokumentInfoRelasjonRepository.findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(
						requestTo.getJournalpostId(), requestTo.getDokumentInfoId()).orElse(null);

		if (relasjonSomSkalSlettesFysisk == null) {
			throw new JournalpostDokumentInfoRelasjonIkkeFunnetException(
					String.format("Kan ikke finne noen relasjon mellom journalpost med journalpostId=%s og dokument med dokumentInfoId=%s",
							requestTo.getJournalpostId(),
							requestTo.getDokumentInfoId()));
		}

		switch (relasjonSomSkalSlettesFysisk.getTilknyttetJournalpostSom()) {
			case HOVEDDOKUMENT:
				sjekkAtJournalpostErUtilgjengeliggjort(relasjonSomSkalSlettesFysisk.getJournalpost().getJournalpostId());
				begrensningService.deleteValidertJournalpostBegrensning(
						relasjonSomSkalSlettesFysisk.getJournalpost().getJournalpostId(),
						BegrensningTypeCode.UTILGJENGELIGGJORT);
				fysiskSlettEtHoveddokument(relasjonSomSkalSlettesFysisk);
				log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + " har fysisk slettet journalpost med journalpostId={}",
						requestTo.getJournalpostId());
				break;
			case VEDLEGG:
				sjekkAtDokumentErUtilgjengeliggjort(
						relasjonSomSkalSlettesFysisk.getJournalpost().getJournalpostId(),
						relasjonSomSkalSlettesFysisk.getDokumentInfo().getDokumentInfoId());
				begrensningService.deleteValidertJournalpostDokumentInfoRelasjonBegrensning(
						relasjonSomSkalSlettesFysisk.getJournalpost().getJournalpostId(),
						relasjonSomSkalSlettesFysisk.getDokumentInfo().getDokumentInfoId(),
						BegrensningTypeCode.UTILGJENGELIGGJORT);
				fysiskSlettEtVedlegg(relasjonSomSkalSlettesFysisk);
				log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) +
								" har fysisk slettet dokument med journalpostId={}, dokumentInfoId={}",
						requestTo.getJournalpostId(), requestTo.getDokumentInfoId());
				break;
			default:
				throw new UgyldigTilknyttetJournalpostSomException(String.format(
						"Kan ikke fysisk slette dokument med journalpostId=%s, dokumentInfoId=%s fordi " +
								"dokumentet er ikke tilknyttet journalposten som hoveddokument eller vedlegg.",
						requestTo.getJournalpostId(),
						requestTo.getDokumentInfoId()));
		}

		return FysiskSlettDokumentResponseMapper.mapToFysiskSlettDokumentResponse(relasjonSomSkalSlettesFysisk);
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
		slettEventuelleVedleggKnyttetHoveddokumentValidertForSletting(relasjonSomSkalSlettes);
		if (relasjonSomSkalSlettes.getDokumentInfo().isRelatedToMultipleJournalposts()) {
			slettJournalpostOgJournalpostDokumentInfoRelasjon(relasjonSomSkalSlettes);
		} else {
			slettJournalpostOgDokumentInfoOgJournalpostDokumentInfoRelasjon(relasjonSomSkalSlettes);
		}
	}

	private void slettEventuelleVedleggKnyttetHoveddokumentValidertForSletting(JournalpostDokumentInfoRelasjon hoveddokumentRelasjon) {
		List<JournalpostDokumentInfoRelasjon> listFoundByJournalpostId =
				journalpostDokumentInfoRelasjonRepository.findAllByJournalpostJournalpostId(hoveddokumentRelasjon.getJournalpost()
						.getJournalpostId());

		Long jpIdTilJpSomSkalSlettes = hoveddokumentRelasjon.getJournalpost().getJournalpostId();

		for (JournalpostDokumentInfoRelasjon relasjon : listFoundByJournalpostId) {
			if (relasjon.isVedlegg()) {
				if (relasjon.getDokumentInfo().isRelatedToMultipleJournalposts() &&
						relasjon.getDokumentInfo()
								.getOriginalJournalpost()
								.getJournalpostId()
								.equals(jpIdTilJpSomSkalSlettes)) {
					endreOriginalJournalpostIDokumentInfo(relasjon.getDokumentInfo(), jpIdTilJpSomSkalSlettes);
				}
				fysiskSlettEtVedlegg(relasjon);
			}
		}
	}

	private void fysiskSlettEtVedlegg(JournalpostDokumentInfoRelasjon relasjonSomSkalSlettes) {
		if (relasjonSomSkalSlettes.getDokumentInfo().isRelatedToMultipleJournalposts()) {
			slettJournalpostDokumentInfoRelasjon(relasjonSomSkalSlettes);
		} else {
			slettFilOgDokumentInfo(relasjonSomSkalSlettes.getDokumentInfo().getDokumentInfoId());
		}
	}

	private void slettJournalpostOgJournalpostDokumentInfoRelasjon(JournalpostDokumentInfoRelasjon relasjon) {
		endreOriginalJournalpostIDokumentInfo(relasjon.getDokumentInfo(), relasjon.getJournalpost().getJournalpostId());
		deleteRepository.deleteJournalpostDokumentInfoRelasjonByJournalpostIdAndDokumentInfoId(
				relasjon.getJournalpost().getJournalpostId(),
				relasjon.getDokumentInfo().getDokumentInfoId());
		slettJournalpost(relasjon.getJournalpost().getJournalpostId());
	}

	private void endreOriginalJournalpostIDokumentInfo(DokumentInfo dokInfoMedJpSomSkalSlettes, Long jpIdTilJpSomSkalSlettes) {
		Journalpost nyOriginalJournalpost = null;
		for (JournalpostDokumentInfoRelasjon relasjon : dokInfoMedJpSomSkalSlettes.getJournalpostRelasjoner()) {
			if (nyOriginalJournalpost == null &&
					isFalse(relasjon.getJournalpost().getJournalpostId().equals(jpIdTilJpSomSkalSlettes))) {
				nyOriginalJournalpost = relasjon.getJournalpost();
			}
		}
		dokInfoMedJpSomSkalSlettes.setOriginalJournalpost(nyOriginalJournalpost);
	}

	private void slettJournalpostOgDokumentInfoOgJournalpostDokumentInfoRelasjon(JournalpostDokumentInfoRelasjon relasjon) {
		slettFilOgDokumentInfo(relasjon.getDokumentInfo().getDokumentInfoId());
		slettJournalpost(relasjon.getJournalpost().getJournalpostId());
	}

	private void slettJournalpostDokumentInfoRelasjon(JournalpostDokumentInfoRelasjon relasjon) {
		deleteRepository.deleteJournalpostDokumentInfoRelasjonByJournalpostIdAndDokumentInfoId(
				relasjon.getJournalpost().getJournalpostId(),
				relasjon.getDokumentInfo().getDokumentInfoId());
	}

	private void slettJournalpost(Long journalpostId) {
		deleteRepository.deleteJPTilleggByJournalpostId(journalpostId);
		deleteRepository.deleteSaksrelasjonByJournalpostId(journalpostId);
		deleteRepository.deleteBrukerByJournalpostId(journalpostId);
		deleteRepository.deleteJournalpostByJournalpostId(journalpostId);
	}

	private void slettFilOgDokumentInfo(Long dokumentInfoId) {
		slettFilBeholdDokumentInfo(dokumentInfoId);
		deleteRepository.deleteSkannetInnholdByDokumentInfoId(dokumentInfoId);
		deleteRepository.deleteDokInfoTilleggByDokumentInfoId(dokumentInfoId);
		deleteRepository.deleteDokInfoJPRelByDokumentInfoId(dokumentInfoId);
		deleteRepository.deleteDokInfoByDokumentInfoId(dokumentInfoId);
	}

	private void slettFilBeholdDokumentInfo(Long dokumentInfoId) {
		deleteRepository.deleteDokumentFilByDokumentInfoId(dokumentInfoId);
		deleteRepository.deleteFilDetaljerByDokumentInfoId(dokumentInfoId);
	}
}
