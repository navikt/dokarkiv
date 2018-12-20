package no.nav.dokarkiv.logiskslettdokument.rjoark100;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.entities.Begrensning;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.service.BegrensningService;
import no.nav.dokarkiv.core.exceptions.DokumentAlleredeUtilgjengeliggjortException;
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
	private final BegrensningService begrensningService;

	@Inject
	public LogiskSlettDokumentService(JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository,
									  BegrensningService begrensningService) {
		this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
		this.begrensningService = begrensningService;
	}

	public LogiskSlettDokumentResponse logiskSletteDokument(LogiskSlettDokumentRequestTo requestTo) {
		sjekkAtDokumentIkkeErUtilgjengeliggjort(requestTo.getJournalpostId(), requestTo.getDokumentInfoId());

		JournalpostDokumentInfoRelasjon relasjonSomSkalSlettesLogisk =
				journalpostDokumentInfoRelasjonRepository.findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(
						requestTo.getJournalpostId(), requestTo.getDokumentInfoId()).orElse(null);

		if (relasjonSomSkalSlettesLogisk == null) {
			throw new JournalpostDokumentInfoRelasjonIkkeFunnetException(
					String.format("Kan ikke finne noen relasjon mellom journalpost med journalpostId=%s og dokument med dokumentInfoId=%s",
							requestTo.getJournalpostId(),
							requestTo.getDokumentInfoId()));
		}

		switch (relasjonSomSkalSlettesLogisk.getTilknyttetJournalpostSom()) {
			case HOVEDDOKUMENT:
				utilgjengeliggjoerHoveddokument(relasjonSomSkalSlettesLogisk.getJournalpost().getJournalpostId());
				log.info("{} har utført logisk sletting av hoveddokument med journalpostId={}",
						MDC.get(MDCConstants.MDC_REQUEST_ID), requestTo.getJournalpostId());
				break;
			case VEDLEGG:
				utilgjengeliggjoerVedlegg(
						relasjonSomSkalSlettesLogisk.getJournalpost().getJournalpostId(),
						relasjonSomSkalSlettesLogisk.getDokumentInfo().getDokumentInfoId());
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

	private void sjekkAtDokumentIkkeErUtilgjengeliggjort(Long journalpostId, Long dokumentInfoId) {
		sjekkAtJournalpostIkkeErUtilgjengeliggjort(journalpostId);
		if (begrensningService.isJournalpostDokumentInfoRelasjonBegrenset(journalpostId, dokumentInfoId, BegrensningTypeCode.UTILGJENGELIGGJORT)) {
			throw new DokumentAlleredeUtilgjengeliggjortException(String.format(
					"Kan ikke utføre logisk sletting av dokument med journalpostId=%s og dokumentInfoId=%s. Dokumentet er utilgjengeliggjort.",
					journalpostId,
					dokumentInfoId));
		}
	}

	private void sjekkAtJournalpostIkkeErUtilgjengeliggjort(Long journalpostId) {
		if (begrensningService.isJournalpostBegrenset(journalpostId, BegrensningTypeCode.UTILGJENGELIGGJORT)) {
			throw new DokumentAlleredeUtilgjengeliggjortException(String.format(
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
