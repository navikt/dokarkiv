package no.nav.dokarkiv.logiskslettdokument.rjoark101;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.exceptions.JournalpostDokumentInfoRelasjonIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.SkjermingIkkeFunnetException;
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
				sjekkAtJournalpostErSkjermet(relasjonDerSlettingSkalAngres.getJournalpost());
				skjermingService.setJournalpostBegrensning(
						relasjonDerSlettingSkalAngres.getJournalpost(),
						null);
				arkivElementEndringTOList.add(
						ArkivElementEndringTO.builder()
								.arkivElement("Journalpost.skjermingType")
								.fraVerdi(SkjermingTypeCode.POL.name())
								.tilVerdi(null)
								.build()
				log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + " har angret logisk sletting av journalpost med journalpostId={}",
						journalpostId);
				break;
			case VEDLEGG:
				sjekkAtDokumentErSkjermet(
						relasjonDerSlettingSkalAngres);
				skjermingService.setJpDokInfoRelBegrensning(
						relasjonDerSlettingSkalAngres,
						null);
				arkivElementEndringTOList.add(
						ArkivElementEndringTO.builder()
								.arkivElement("DokumentInfo.skjermingType")
								.fraVerdi(SkjermingTypeCode.POL.name())
								.tilVerdi(null)
								.build()
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

	private void sjekkAtDokumentErSkjermet(JournalpostDokumentInfoRelasjon rel) {
		if (!SkjermingTypeCode.POL.equals(rel.getSkjermingType())) {
			throw new SkjermingIkkeFunnetException(String.format(
					"Fant ikke forventet skjerming for dokument med journalpostId=%s, dokumentInfoId=%s og skjermingType=%s. Det kan hende journalpost med journalpostId=%s er allerede utilgjengeliggjort.",
					rel.getJournalpost().getJournalpostId(),
					rel.getDokumentInfo().getDokumentInfoId(),
					SkjermingTypeCode.POL.name(), rel.getJournalpost().getJournalpostId()));
		}
	}

	private void sjekkAtJournalpostErSkjermet(Journalpost journalpost) {
		if (!SkjermingTypeCode.POL.equals(journalpost.getSkjermingType())) {
			throw new SkjermingIkkeFunnetException(String.format(
					"Fant ikke forventet skjerming for journalpost med journalpostId=%s og skjermingType=%s.",
					journalpost.getJournalpostId(),
					SkjermingTypeCode.POL.name()));
		}
	}
}
