package no.nav.dokarkiv.logiskslettdokument.rjoark101;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.service.BegrensningService;
import no.nav.dokarkiv.core.exceptions.BegrensningIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostDokumentInfoRelasjonIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.UgyldigTilknyttetJournalpostSomException;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import no.nav.dokarkiv.logiskslettdokument.rjoark100.LogiskSlettDokumentRequestTo;
import no.nav.dokarkiv.logiskslettdokument.rjoark100.LogiskSlettDokumentResponse;
import no.nav.dokarkiv.logiskslettdokument.rjoark100.LogiskSlettDokumentResponseMapper;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
@Slf4j
public class AngreLogiskSlettDokumentService {

	private final JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;
	private final BegrensningService begrensningService;

	@Inject
	public AngreLogiskSlettDokumentService(JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository,
										   BegrensningService begrensningService) {
		this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
		this.begrensningService = begrensningService;
	}

	public LogiskSlettDokumentResponse angreLogiskSlettDokument(LogiskSlettDokumentRequestTo requestTo) {
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

		switch (relasjonDerSlettingSkalAngres.getTilknyttetJournalpostSom()) {
			case HOVEDDOKUMENT:
				sjekkAtJournalpostErUtilgjengeliggjort(relasjonDerSlettingSkalAngres.getJournalpost());
				begrensningService.setJournalpostBegrensning(
						relasjonDerSlettingSkalAngres.getJournalpost(),
						null);
				log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + " har angret logisk sletting av journalpost med journalpostId={}",
						journalpostId);
				break;
			case VEDLEGG:
				sjekkAtDokumentErUtilgjengeliggjort(
						relasjonDerSlettingSkalAngres);
				begrensningService.setJpDokInfoRelBegrensning(
						relasjonDerSlettingSkalAngres,
						null);
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

		return LogiskSlettDokumentResponseMapper.mapToSlettDokumentResponse(relasjonDerSlettingSkalAngres);
	}

	private void sjekkAtDokumentErUtilgjengeliggjort(JournalpostDokumentInfoRelasjon rel) {
		if (!BegrensningTypeCode.POL.equals(rel.getBegrensning())) {
			throw new BegrensningIkkeFunnetException(String.format(
					"Fant ikke forventet begrensning for dokument med journalpostId=%s, dokumentInfoId=%s og begrensningsType=%s. Det kan hende journalpost med journalpostId=%s er allerede utilgjengeliggjort.",
					rel.getJournalpost().getJournalpostId(),
					rel.getDokumentInfo().getDokumentInfoId(),
					BegrensningTypeCode.POL.name(), rel.getJournalpost().getJournalpostId()));
		}
	}

	private void sjekkAtJournalpostErUtilgjengeliggjort(Journalpost journalpost) {
		if (!BegrensningTypeCode.POL.equals(journalpost.getBegrensning())) {
			throw new BegrensningIkkeFunnetException(String.format(
					"Fant ikke forventet begrensning for journalpost med journalpostId=%s og begrensningsType=%s.",
					journalpost.getJournalpostId(),
					BegrensningTypeCode.POL.name()));
		}
	}
}
