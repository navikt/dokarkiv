package no.nav.dokarkiv.logiskslettdokument.rjoark101;

import static org.apache.commons.lang3.BooleanUtils.isFalse;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.service.BegrensningService;
import no.nav.dokarkiv.core.exceptions.DokumentInfoIkkeTilknyttetJournalpostSomGyldigVerdiException;
import no.nav.dokarkiv.core.exceptions.JournalpostDokumentInfoRelasjonIkkeFunnetException;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import no.nav.dokarkiv.logiskslettdokument.exceptions.BegrensningIkkeFunnetException;
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
				sjekkAtJournalpostErUtilgjengeliggjort(journalpostId);
				begrensningService.deleteValidertJournalpostBegrensning(
						journalpostId,
						BegrensningTypeCode.UTILGJENGELIGGJORT);
				log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + " har angret logisk sletting av journalpost med journalpostId={}",
						journalpostId);
				break;
			case VEDLEGG:
				sjekkAtDokumentErUtilgjengeliggjort(
						journalpostId,
						dokumentInfoId);
				begrensningService.deleteValidertJournalpostDokumentInfoRelasjonBegrensning(
						journalpostId,
						dokumentInfoId,
						BegrensningTypeCode.UTILGJENGELIGGJORT);
				log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) +
								" har angret logisk sletting av dokument med journalpostId={}, dokumentInfoId={}",
						journalpostId, dokumentInfoId);
				break;
			default:
				throw new DokumentInfoIkkeTilknyttetJournalpostSomGyldigVerdiException(String.format(
						"Dokument med dokumentInfoId=%s er tilknyttet journalpost med journalpostId=%s som %s. " +
								"Gyldige verdier er %s eller %s.",
						dokumentInfoId,
						journalpostId,
						relasjonDerSlettingSkalAngres.getTilknyttetJournalpostSom().name(),
						TilknyttetJournalpostSomCode.HOVEDDOKUMENT.name(),
						TilknyttetJournalpostSomCode.VEDLEGG.name()));
		}

		return LogiskSlettDokumentResponseMapper.mapToSlettDokumentResponse(relasjonDerSlettingSkalAngres);
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
}
