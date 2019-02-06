package no.nav.dokarkiv.logiskslettdokument.rjoark100;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.entities.ArkivElementEndring;
import no.nav.dokarkiv.core.domain.entities.Begrensning;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.exceptions.DokumentAlleredeSkjermetException;
import no.nav.dokarkiv.core.exceptions.JournalpostDokumentInfoRelasjonIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.UgyldigTilknyttetJournalpostSomException;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

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

	public List<ArkivElementEndringTO> logiskSletteDokument(LogiskSlettDokumentRequestTo requestTo) {
		sjekkAtDokumentIkkeErPOL(requestTo.getJournalpostId(), requestTo.getDokumentInfoId());

		JournalpostDokumentInfoRelasjon relasjonSomSkalSlettesLogisk =
				journalpostDokumentInfoRelasjonRepository.findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(
						requestTo.getJournalpostId(), requestTo.getDokumentInfoId()).orElse(null);

		if (relasjonSomSkalSlettesLogisk == null) {
			throw new JournalpostDokumentInfoRelasjonIkkeFunnetException(
					String.format("Kan ikke finne noen relasjon mellom journalpost med journalpostId=%s og dokument med dokumentInfoId=%s",
							requestTo.getJournalpostId(),
							requestTo.getDokumentInfoId()));
		}

		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();
		switch (relasjonSomSkalSlettesLogisk.getTilknyttetJournalpostSom()) {
			case HOVEDDOKUMENT:
				utilgjengeliggjoerHoveddokument(relasjonSomSkalSlettesLogisk.getJournalpost().getJournalpostId());
				arkivElementEndringTOList.add(
						ArkivElementEndringTO.builder()
								.arkivElement("Journalpost.skjermingType")
								.fraVerdi(null)
								.tilVerdi("POL")
								.build()
				);
				log.info("{} har utført logisk sletting av hoveddokument med journalpostId={}",
						MDC.get(MDCConstants.MDC_REQUEST_ID), requestTo.getJournalpostId());
				break;
			case VEDLEGG:
				utilgjengeliggjoerVedlegg(
						relasjonSomSkalSlettesLogisk.getJournalpost().getJournalpostId(),
						relasjonSomSkalSlettesLogisk.getDokumentInfo().getDokumentInfoId());
				arkivElementEndringTOList.add(
						ArkivElementEndringTO.builder()
								.arkivElement("DokumentInfo.skjermingType")
								.fraVerdi(null)
								.tilVerdi("POL")
								.build()
				);
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

		return arkivElementEndringTOList;
	}

	private void sjekkAtDokumentIkkeErPOL(Long journalpostId, Long dokumentInfoId) {
		sjekkAtJournalpostIkkeErPOL(journalpostId);
		if (skjermingService.isJournalpostDokumentInfoRelasjonSkjermet(journalpostId, dokumentInfoId, SkjermingTypeCode.POL)) {
			throw new DokumentAlleredeSkjermetException(String.format(
					"Kan ikke utføre logisk sletting av dokument med journalpostId=%s og dokumentInfoId=%s. Dokumentet er skjermet.",
					journalpostId,
					dokumentInfoId));
		}
	}

	private void sjekkAtJournalpostIkkeErPOL(Long journalpostId) {
		if (skjermingService.isJournalpostSkjermet(journalpostId, SkjermingTypeCode.POL)) {
			throw new DokumentAlleredeSkjermetException(String.format(
					"Kan ikke utføre logisk sletting av dokument med journalpostId=%s. Journalposten er skjermet",
					journalpostId));
		}
	}

	private void utilgjengeliggjoerHoveddokument(Long journalpostId) {
		Begrensning begrensning = Begrensning.builder()
				.begrensningType(SkjermingTypeCode.POL)
				.journalpostId(journalpostId)
				.build();
		begrensning.setOpprettetKildeNavn(MDC.get(MDCConstants.MDC_CONSUMER_ID));

		skjermingService.saveBegrensning(begrensning);
	}

	private void utilgjengeliggjoerVedlegg(Long journalpostId, Long dokumentInfoId) {
		Begrensning begrensning = Begrensning.builder()
				.begrensningType(SkjermingTypeCode.POL)
				.journalpostId(journalpostId)
				.dokumentInfoId(dokumentInfoId)
				.build();
		begrensning.setOpprettetKildeNavn(MDC.get(MDCConstants.MDC_CONSUMER_ID));

		skjermingService.saveBegrensning(begrensning);
	}
}
