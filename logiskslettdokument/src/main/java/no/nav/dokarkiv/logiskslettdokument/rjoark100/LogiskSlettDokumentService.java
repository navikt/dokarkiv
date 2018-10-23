package no.nav.dokarkiv.logiskslettdokument.rjoark100;

import static no.nav.dokarkiv.logiskslettdokument.common.Slettemelding.setDokumentLogiskSlettet;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
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
	private final DokumentinfoRepository dokumentinfoRepository;
	private final JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;

	@Inject
	public LogiskSlettDokumentService(LogiskSlettDokumentValidator validator,
									  DokumentinfoRepository dokumentinfoRepository,
									  JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository) {
		this.validator = validator;
		this.dokumentinfoRepository = dokumentinfoRepository;
		this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
	}

	public LogiskSlettDokumentResponse logiskSlettDokumentKnyttetKunEnJournalpost(LogiskSlettDokumentRequestTo requestTo) {
		List<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjonList =
				journalpostDokumentInfoRelasjonRepository.findByDokumentInfoId(requestTo.getDokumentInfoId())
						.orElse(new ArrayList<>());

		validator.validerAtDokumentSomSkalSlettesLogiskErKnyttetTilKunEnJournalpost(journalpostDokumentInfoRelasjonList, requestTo);
		JournalpostDokumentInfoRelasjon validertJpDokInfoRelasjon = journalpostDokumentInfoRelasjonList.get(0);

		setDokumentLogiskSlettet(validertJpDokInfoRelasjon.getDokumentInfo());
		dokumentinfoRepository.save(validertJpDokInfoRelasjon.getDokumentInfo());
		log.info("{} har utført logisk sletting av dokument med journalpostId={}, dokumentInfoId={}",
				MDC.get(MDCConstants.MDC_REQUEST_ID), requestTo.getJournalpostId(), requestTo.getDokumentInfoId());

		return LogiskSlettDokumentResponseMapper.mapToSlettDokumentResponse(validertJpDokInfoRelasjon.getJournalpost(),
				validertJpDokInfoRelasjon.getDokumentInfo());
	}
}
