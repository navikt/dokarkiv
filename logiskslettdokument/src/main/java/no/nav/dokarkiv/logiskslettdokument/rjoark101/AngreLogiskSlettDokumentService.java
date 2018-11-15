package no.nav.dokarkiv.logiskslettdokument.rjoark101;

import static org.apache.commons.lang3.BooleanUtils.isFalse;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.service.BegrensningService;
import no.nav.dokarkiv.core.exceptions.BegrensningIkkeFunnetException;
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

	private final AngreLogiskSlettDokumentValidator validator;
	private final JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;
	private final BegrensningService begrensningService;

	@Inject
	public AngreLogiskSlettDokumentService(AngreLogiskSlettDokumentValidator validator,
										   JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository,
										   BegrensningService begrensningService) {
		this.validator = validator;
		this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
		this.begrensningService = begrensningService;
	}

	public LogiskSlettDokumentResponse angreLogiskSlettDokument(LogiskSlettDokumentRequestTo requestTo) {
		List<JournalpostDokumentInfoRelasjon> relasjonerDerSlettingSkalAngres =
				journalpostDokumentInfoRelasjonRepository.findAllByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(
						requestTo.getJournalpostId(), requestTo.getDokumentInfoId()).orElse(new ArrayList<>());

		validator.validerAngreLogiskSlettAvEttDokument(relasjonerDerSlettingSkalAngres, requestTo);

		JournalpostDokumentInfoRelasjon angreSlettRelasjon = relasjonerDerSlettingSkalAngres.get(0);

		if (TilknyttetJournalpostSomCode.HOVEDDOKUMENT.equals(angreSlettRelasjon.getTilknyttetJournalpostSom())) {
			sjekkAtJournalpostErUtilgjengeliggjort(angreSlettRelasjon.getJournalpost().getJournalpostId());
			begrensningService.deleteValidertJournalpostBegrensning(
					requestTo.getJournalpostId(),
					BegrensningTypeCode.UTILGJENGELIGGJORT);
			log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + " har angret logisk sletting av journalpost med journalpostId={}",
					requestTo.getJournalpostId());
		} else {
			sjekkAtDokumentErUtilgjengeliggjort(
					angreSlettRelasjon.getJournalpost().getJournalpostId(),
					angreSlettRelasjon.getDokumentInfo().getDokumentInfoId());
			begrensningService.deleteValidertJournalpostDokumentInfoRelasjonBegrensning(
					requestTo.getJournalpostId(),
					requestTo.getDokumentInfoId(),
					BegrensningTypeCode.UTILGJENGELIGGJORT);
			log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) +
							" har angret logisk sletting av dokument med journalpostId={}, dokumentInfoId={}",
					requestTo.getJournalpostId(), requestTo.getDokumentInfoId());
		}

		return LogiskSlettDokumentResponseMapper.mapToSlettDokumentResponse(angreSlettRelasjon);
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
