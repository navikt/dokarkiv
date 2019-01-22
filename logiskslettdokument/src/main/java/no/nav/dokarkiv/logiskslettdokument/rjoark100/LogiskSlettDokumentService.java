package no.nav.dokarkiv.logiskslettdokument.rjoark100;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.exceptions.DokumentAlleredeSkjermetException;
import no.nav.dokarkiv.core.exceptions.JournalpostDokumentInfoRelasjonIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.UgyldigTilknyttetJournalpostSomException;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
@Slf4j
public class LogiskSlettDokumentService {

	private final JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;
	private final SkjermingService skjermingService;

	@Inject
	public LogiskSlettDokumentService(JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository,
									  SkjermingService skjermingService) {
		this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
		this.skjermingService = skjermingService;
	}

	public LogiskSlettDokumentResponse logiskSletteDokument(LogiskSlettDokumentRequestTo requestTo) {
		sjekkAtDokumentIkkeErSkjermet(requestTo.getJournalpostId(), requestTo.getDokumentInfoId());

		JournalpostDokumentInfoRelasjon relasjonSomSkalSlettesLogisk =
				journalpostDokumentInfoRelasjonRepository.findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(
						requestTo.getJournalpostId(), requestTo.getDokumentInfoId()).orElse(null);

		if (relasjonSomSkalSlettesLogisk == null) {
			throw new JournalpostDokumentInfoRelasjonIkkeFunnetException(
					String.format("Kan ikke finne noen relasjon mellom journalpost med journalpostId=%s og dokument med dokumentInfoId=%s",
							requestTo.getJournalpostId(),
							requestTo.getDokumentInfoId()));
		}
		if (SkjermingTypeCode.POL.equals(relasjonSomSkalSlettesLogisk.getJournalpost().getSkjermingType()) || SkjermingTypeCode.POL.equals(relasjonSomSkalSlettesLogisk.getSkjermingType())) {
			throw new DokumentAlleredeSkjermetException(String.format(
					"Kan ikke utføre logisk sletting av dokument med journalpostId=%s og dokumentInfoId=%s. Dokumentet er skjermet.",
					requestTo.getJournalpostId(),
					requestTo.getDokumentInfoId()));
		}

		switch (relasjonSomSkalSlettesLogisk.getTilknyttetJournalpostSom()) {
			case HOVEDDOKUMENT:
				skjermingService.setJournalpostBegrensning (relasjonSomSkalSlettesLogisk.getJournalpost(), SkjermingTypeCode.POL);
				log.info("{} har utført logisk sletting av hoveddokument med journalpostId={}",
						MDC.get(MDCConstants.MDC_REQUEST_ID), requestTo.getJournalpostId());
				break;
			case VEDLEGG:
				skjermingService.setJpDokInfoRelBegrensning(
						relasjonSomSkalSlettesLogisk, SkjermingTypeCode.POL);
				log.info("{} har utført logisk sletting av vedlegg med journalpostId={} og dokumentInfoId={}",
						MDC.get(MDCConstants.MDC_REQUEST_ID), requestTo.getJournalpostId(), requestTo.getDokumentInfoId());
				break;
			default:
				throw new UgyldigTilknyttetJournalpostSomException(String.format(
						"Kan ikke logisk slette dokument med journalpostId=%s, dokumentInfoId=%s fordi " +
								"dokumentet er ikke tilknyttet journalposten som hoveddokument eller vedlegg.",
						requestTo.getJournalpostId(),
						requestTo.getDokumentInfoId()));
		}

		return LogiskSlettDokumentResponseMapper.mapToSlettDokumentResponse(relasjonSomSkalSlettesLogisk);
	}

	private void sjekkAtDokumentIkkeErSkjermet(Long journalpostId, Long dokumentInfoId) {
		sjekkAtJournalpostIkkeErSkjermet(journalpostId);
		if (skjermingService.isJournalpostDokumentInfoRelasjonSkjermet(journalpostId, dokumentInfoId, SkjermingTypeCode.POL)) {
			throw new DokumentAlleredeSkjermetException(String.format(
					"Kan ikke utføre logisk sletting av dokument med journalpostId=%s og dokumentInfoId=%s. Dokumentet er skjermet.",
					journalpostId,
					dokumentInfoId));
		}
	}

	private void sjekkAtJournalpostIkkeErSkjermet(Long journalpostId) {
		if (skjermingService.isJournalpostSkjermet(journalpostId, SkjermingTypeCode.POL)) {
			throw new DokumentAlleredeSkjermetException(String.format(
					"Kan ikke utføre logisk sletting av dokument med journalpostId=%s. Journalposten er skjermet",
					journalpostId));
		}
	}

//	private void utilgjengeliggjoerHoveddokument(Long journalpostId) {
//		Begrensning begrensning = Begrensning.builder()
//				.begrensningType(SkjermingTypeCode.UTILGJENGELIGGJORT)
//				.journalpostId(journalpostId)
//				.build();
//		begrensning.setOpprettetKildeNavn(MDC.get(MDCConstants.MDC_CONSUMER_ID));
//
//		skjermingService.saveBegrensning(begrensning);
//	}
//
//	private void utilgjengeliggjoerVedlegg(Long journalpostId, Long dokumentInfoId) {
//		Begrensning begrensning = Begrensning.builder()
//				.begrensningType(SkjermingTypeCode.UTILGJENGELIGGJORT)
//				.journalpostId(journalpostId)
//				.dokumentInfoId(dokumentInfoId)
//				.build();
//		begrensning.setOpprettetKildeNavn(MDC.get(MDCConstants.MDC_CONSUMER_ID));
//
//		skjermingService.saveBegrensning(begrensning);
//	}
}
