package no.nav.dokarkiv.fysiskslettdokument.rjoark102;

import static org.apache.commons.lang3.BooleanUtils.isFalse;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.exceptions.SkjermingIkkeFunnetException;
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
	private final SkjermingService skjermingService;

	@Inject
	public FysiskSlettDokumentService(
			JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository,
			JoarkDeleteRepository deleteRepository,
			SkjermingService skjermingService) {
		this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
		this.deleteRepository = deleteRepository;
		this.skjermingService = skjermingService;
	}

	public FysiskSlettDokumentResponse sletteDokumentFysisk(FysiskSlettDokumentRequestTo requestTo) {
		JournalpostDokumentInfoRelasjon relasjonSomSkalSlettesFysisk =
				journalpostDokumentInfoRelasjonRepository.findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(
						requestTo.getJournalpostId(), requestTo.getDokumentInfoId()).orElseThrow(() ->
						new JournalpostDokumentInfoRelasjonIkkeFunnetException(String.format(
								"Kan ikke finne noen relasjon mellom journalpost med journalpostId=%s og dokument med dokumentInfoId=%s",
								requestTo.getJournalpostId(),
								requestTo.getDokumentInfoId())));

		switch (relasjonSomSkalSlettesFysisk.getTilknyttetJournalpostSom()) {
			case HOVEDDOKUMENT:
				sjekkAtJournalpostErPOL(relasjonSomSkalSlettesFysisk.getJournalpost().getJournalpostId());
				skjermingService.deleteValidertJournalpostBegrensning(
						relasjonSomSkalSlettesFysisk.getJournalpost().getJournalpostId(),
						SkjermingTypeCode.POL);
				fysiskSlettEtHoveddokument(relasjonSomSkalSlettesFysisk);
				log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + " har fysisk slettet journalpost med journalpostId={}",
						requestTo.getJournalpostId());
				break;
			case VEDLEGG:
				sjekkAtDokumentErPOL(
						relasjonSomSkalSlettesFysisk.getJournalpost().getJournalpostId(),
						relasjonSomSkalSlettesFysisk.getDokumentInfo().getDokumentInfoId());
				skjermingService.deleteValidertJournalpostDokumentInfoRelasjonBegrensning(
						relasjonSomSkalSlettesFysisk.getJournalpost().getJournalpostId(),
						relasjonSomSkalSlettesFysisk.getDokumentInfo().getDokumentInfoId(),
						SkjermingTypeCode.POL);
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

	private void sjekkAtJournalpostErPOL(Long journalpostId) {
		if (isFalse(skjermingService.isJournalpostSkjermet(
				journalpostId,
				SkjermingTypeCode.POL))) {
			throw new SkjermingIkkeFunnetException(String.format(
					"Fant ikke forventet begrensning for journalpost med journalpostId=%s og begrensningsType=%s.",
					journalpostId,
					SkjermingTypeCode.POL.name()));
		}
	}

	private void sjekkAtDokumentErPOL(Long journalpostId, Long dokumentInfoId) {
		if (isFalse(skjermingService.isJournalpostDokumentInfoRelasjonSkjermet(
				journalpostId,
				dokumentInfoId,
				SkjermingTypeCode.POL))) {
			throw new SkjermingIkkeFunnetException(String.format(
					"Fant ikke forventet begrensning for dokument med journalpostId=%s, dokumentInfoId=%s og begrensningsType=%s.",
					journalpostId,
					dokumentInfoId,
					SkjermingTypeCode.POL.name()));
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
				Long originalJournalpostId = relasjon.getDokumentInfo().getOriginalJournalpost() ==
						null ? -1 : relasjon.getDokumentInfo().getOriginalJournalpost().getJournalpostId();
				if (relasjon.getDokumentInfo().isRelatedToMultipleJournalposts() &&
						originalJournalpostId.equals(jpIdTilJpSomSkalSlettes)) {
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
