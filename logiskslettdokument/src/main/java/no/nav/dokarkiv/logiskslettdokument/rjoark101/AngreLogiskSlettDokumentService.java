package no.nav.dokarkiv.logiskslettdokument.rjoark101;

import static no.nav.dokarkiv.logiskslettdokument.common.Slettemelding.setAngreDokumentLogiskSlettet;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
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
	private final DokumentinfoRepository dokumentinfoRepository;
	private final JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;

	@Inject
	public AngreLogiskSlettDokumentService(AngreLogiskSlettDokumentValidator validator,
										   DokumentinfoRepository dokumentinfoRepository,
										   JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository) {
		this.validator = validator;
		this.dokumentinfoRepository = dokumentinfoRepository;
		this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
	}

	public LogiskSlettDokumentResponse angreLogiskSlettDokument(LogiskSlettDokumentRequestTo requestTo) {
		List<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjonList =
				journalpostDokumentInfoRelasjonRepository.findByDokumentInfoId(requestTo.getDokumentInfoId())
						.orElse(new ArrayList<>());

		validator.validerAngreLogiskSlettAvEttDokument(journalpostDokumentInfoRelasjonList, requestTo);
		JournalpostDokumentInfoRelasjon validertJpDokInfoRelasjon = journalpostDokumentInfoRelasjonList.get(0);

		setAngreDokumentLogiskSlettet(validertJpDokInfoRelasjon.getDokumentInfo());
		dokumentinfoRepository.save(validertJpDokInfoRelasjon.getDokumentInfo());
		log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + " har angret logisk sletting av dokument med journalpostId={}, dokumentInfoId={}",
				requestTo.getJournalpostId(), requestTo.getDokumentInfoId());

		return LogiskSlettDokumentResponseMapper.mapToSlettDokumentResponse(validertJpDokInfoRelasjon.getJournalpost(),
				validertJpDokInfoRelasjon.getDokumentInfo());
	}
}
