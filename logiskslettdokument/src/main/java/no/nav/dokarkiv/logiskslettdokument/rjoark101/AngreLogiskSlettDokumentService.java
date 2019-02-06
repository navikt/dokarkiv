package no.nav.dokarkiv.logiskslettdokument.rjoark101;

import static org.apache.commons.lang3.BooleanUtils.isFalse;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.exceptions.SkjermingIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostDokumentInfoRelasjonIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.UgyldigTilknyttetJournalpostSomException;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import no.nav.dokarkiv.logiskslettdokument.rjoark100.LogiskSlettDokumentRequestTo;
import no.nav.dokarkiv.logiskslettdokument.rjoark100.LogiskSlettDokumentResponse;
import no.nav.dokarkiv.logiskslettdokument.rjoark100.LogiskSlettDokumentResponseMapper;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class AngreLogiskSlettDokumentService {

	private final JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;
	private final SkjermingService skjermingService;

	@Inject
	public AngreLogiskSlettDokumentService(JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository,
										   SkjermingService skjermingService) {
		this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
		this.skjermingService = skjermingService;
	}

	public List<ArkivElementEndringTO> angreLogiskSlettDokument(LogiskSlettDokumentRequestTo requestTo) {
		JournalpostDokumentInfoRelasjon relasjonDerSlettingSkalAngres =
				journalpostDokumentInfoRelasjonRepository.findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(
						requestTo.getJournalpostId(), requestTo.getDokumentInfoId()).orElse(null);

		if (relasjonDerSlettingSkalAngres == null) {
			throw new JournalpostDokumentInfoRelasjonIkkeFunnetException(
					String.format("Kan ikke finne noen relasjon mellom journalpost med journalpostId=%s og dokument med dokumentInfoId=%s",
							requestTo.getJournalpostId(),
							requestTo.getDokumentInfoId()));
		}

		Long journalpostId = relasjonDerSlettingSkalAngres.getJournalpost().getJournalpostId();
		Long dokumentInfoId = relasjonDerSlettingSkalAngres.getDokumentInfo().getDokumentInfoId();
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();

		switch (relasjonDerSlettingSkalAngres.getTilknyttetJournalpostSom()) {
			case HOVEDDOKUMENT:
				sjekkAtJournalpostErPOL(journalpostId);
				skjermingService.deleteValidertJournalpostBegrensning(
						journalpostId,
						SkjermingTypeCode.POL);
				arkivElementEndringTOList.add(
						ArkivElementEndringTO.builder()
								.arkivElement("Journalpost.skjermingType")
								.fraVerdi(SkjermingTypeCode.POL.name())
								.tilVerdi(null)
								.build()
				);
				log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + " har angret logisk sletting av journalpost med journalpostId={}",
						journalpostId);
				break;
			case VEDLEGG:
				sjekkAtDokumentErPOL(
						journalpostId,
						dokumentInfoId);
				skjermingService.deleteValidertJournalpostDokumentInfoRelasjonBegrensning(
						journalpostId,
						dokumentInfoId,
						SkjermingTypeCode.POL);
				arkivElementEndringTOList.add(
						ArkivElementEndringTO.builder()
								.arkivElement("DokumentInfo.skjermingType")
								.fraVerdi(SkjermingTypeCode.POL.name())
								.tilVerdi(null)
								.build()
				);
				log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) +
								" har angret logisk sletting av dokument med journalpostId={}, dokumentInfoId={}",
						journalpostId, dokumentInfoId);
				break;
			default:
				throw new UgyldigTilknyttetJournalpostSomException(String.format(
						"Kan ikke angre logisk sletting av dokument med journalpostId=%s, dokumentInfoId=%s fordi " +
								"dokumentet er ikke tilknyttet journalposten som hoveddokument eller vedlegg.",
						requestTo.getJournalpostId(),
						requestTo.getDokumentInfoId()));
		}

		return arkivElementEndringTOList;
	}

	private void sjekkAtDokumentErPOL(Long journalpostId, Long dokumentInfoId) {
		if (isFalse(skjermingService.isJournalpostDokumentInfoRelasjonSkjermet(
				journalpostId,
				dokumentInfoId,
				SkjermingTypeCode.POL))) {
			throw new SkjermingIkkeFunnetException(String.format(
					"Fant ikke forventet begrensning for dokument med journalpostId=%s, dokumentInfoId=%s og begrensningsType=%s. Det kan hende journalpost med journalpostId=%s er allerede POL.",
					journalpostId,
					dokumentInfoId,
					SkjermingTypeCode.POL.name(), journalpostId));
		}
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
}
