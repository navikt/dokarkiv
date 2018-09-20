package no.nav.dokarkiv.logiskslettdokument.rjoark100;

import static no.nav.dokarkiv.logiskslettdokument.LogiskSlettDokumentRestController.REQUEST_ID;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * Implementation of SlettDokument
 */
@Service
@Slf4j
public class LogiskSlettDokumentService {

	@Inject
	private LogiskSlettDokumentValidator validator;

	@Inject
	private DokumentinfoRepository dokumentinfoRepository;

	@Inject
	private JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;

	public LogiskSlettDokumentResponse slettDokumentLogisk(LogiskSlettDokumentRequestTo requestTo) {
		JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon = validator.validateLogiskSlettDokument(
				journalpostDokumentInfoRelasjonRepository.findByDokumentInfoId(requestTo.getDokumentInfoId()), requestTo);

		setDokumentLogiskSlettet(journalpostDokumentInfoRelasjon.getDokumentInfo());
		log.info(REQUEST_ID + " har utført logisk sletting av dokument med journalpostId={}, dokumentInfoId={}",
				requestTo.getJournalpostId(), requestTo.getDokumentInfoId());

		return LogiskSlettDokumentResponseMapper.mapToSlettDokumentResponse(journalpostDokumentInfoRelasjon.getJournalpost(),
				journalpostDokumentInfoRelasjon.getDokumentInfo());
	}

	private void setDokumentLogiskSlettet(DokumentInfo dokumentInfo) {
		dokumentInfo.setSlettet(true);
		dokumentInfo.setEndretAvNavn(MDC.get(MDCConstants.MDC_USER_NAME));
		dokumentinfoRepository.save(dokumentInfo);
	}
}
