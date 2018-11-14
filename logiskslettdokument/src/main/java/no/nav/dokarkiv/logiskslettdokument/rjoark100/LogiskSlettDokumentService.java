package no.nav.dokarkiv.logiskslettdokument.rjoark100;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.Begrensning;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.service.BegrensningService;
import no.nav.dokarkiv.core.exceptions.ErBegrensetException;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class LogiskSlettDokumentService {

	private final LogiskSlettDokumentValidator validator;
	private final JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;
	private final BegrensningService begrensningService;

	@Inject
	public LogiskSlettDokumentService(LogiskSlettDokumentValidator validator,
									  JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository,
									  BegrensningService begrensningService) {
		this.validator = validator;
		this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
		this.begrensningService = begrensningService;
	}

	public LogiskSlettDokumentResponse logiskSletteDokumentKnyttetKunEnJournalpost(LogiskSlettDokumentRequestTo requestTo) {
		sjekkAtDokumentIkkeErUtilgjengeliggjort(requestTo.getJournalpostId(), requestTo.getDokumentInfoId());

		List<JournalpostDokumentInfoRelasjon> jpDokInfoRelasjonerFoundByDokumentInfoId =
				journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(requestTo.getDokumentInfoId())
						.orElse(new ArrayList<>());

		validator.validerAtDokumentSomSkalSlettesLogiskErKnyttetTilKunEnJournalpost(jpDokInfoRelasjonerFoundByDokumentInfoId, requestTo);
		JournalpostDokumentInfoRelasjon validertJpDokInfoRelasjon = jpDokInfoRelasjonerFoundByDokumentInfoId.get(0);

		if (validertJpDokInfoRelasjon.getTilknyttetJournalpostSom().equals(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)) {
			utilgjengeliggjoerHoveddokument(validertJpDokInfoRelasjon.getJournalpost().getJournalpostId());
			log.info("{} har utført logisk sletting av hoveddokument med journalpostId={}",
					MDC.get(MDCConstants.MDC_REQUEST_ID), requestTo.getJournalpostId());
		} else {
			utilgjengeliggjoerVedlegg(
					validertJpDokInfoRelasjon.getJournalpost().getJournalpostId(),
					validertJpDokInfoRelasjon.getDokumentInfo().getDokumentInfoId());
			log.info("{} har utført logisk sletting av vedlegg med journalpostId={} og dokumentInfoId={}",
					MDC.get(MDCConstants.MDC_REQUEST_ID), requestTo.getJournalpostId(), requestTo.getDokumentInfoId());
		}

		return LogiskSlettDokumentResponseMapper.mapToSlettDokumentResponse(validertJpDokInfoRelasjon);
	}

	private void sjekkAtDokumentIkkeErUtilgjengeliggjort(Long journalpostId, Long dokumentInfoId) {
		sjekkAtJournalpostIkkeErUtilgjengeliggjort(journalpostId);
		if (begrensningService.isJournalpostDokumentInfoRelasjonBegrenset(journalpostId, dokumentInfoId, BegrensningTypeCode.UTILGJENGELIGGJORT)) {
			throw new ErBegrensetException(String.format(
					"Kan ikke utføre logisk sletting av dokument med journalpostId=%s og dokumentInfoId=%s. Dokumentet er utilgjengeliggjort.",
					journalpostId,
					dokumentInfoId));
		}
	}

	private void sjekkAtJournalpostIkkeErUtilgjengeliggjort(Long journalpostId) {
		if (begrensningService.isJournalpostBegrenset(journalpostId, BegrensningTypeCode.UTILGJENGELIGGJORT)) {
			throw new ErBegrensetException(String.format(
					"Kan ikke utføre logisk sletting av dokument med journalpostId=%s. Journalposten er utilgjengeliggjort",
					journalpostId));
		}
	}

	private void utilgjengeliggjoerHoveddokument(Long journalpostId) {
		Begrensning begrensning = Begrensning.builder()
				.begrensningType(BegrensningTypeCode.UTILGJENGELIGGJORT)
				.journalpostId(journalpostId)
				.build();
		begrensning.setOpprettetKildeNavn(MDC.get(MDCConstants.MDC_CONSUMER_ID));

		begrensningService.saveBegrensning(begrensning);
	}

	private void utilgjengeliggjoerVedlegg(Long journalpostId, Long dokumentInfoId) {
		Begrensning begrensning = Begrensning.builder()
				.begrensningType(BegrensningTypeCode.UTILGJENGELIGGJORT)
				.journalpostId(journalpostId)
				.dokumentInfoId(dokumentInfoId)
				.build();
		begrensning.setOpprettetKildeNavn(MDC.get(MDCConstants.MDC_CONSUMER_ID));

		begrensningService.saveBegrensning(begrensning);
	}
}
