package no.nav.dokarkiv.logiskslettdokument.rjoark100;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class LogiskSlettDokumentService {

	@Value("${logiskslettdokument.slettemelding}")
	private static String SLETTEMELDING;

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

	public LogiskSlettDokumentResponse slettDokumentLogisk(LogiskSlettDokumentRequestTo requestTo) {
		List<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjonList =
				journalpostDokumentInfoRelasjonRepository.findByDokumentInfoId(requestTo.getDokumentInfoId())
						.orElse(new ArrayList<>());

		validator.validateLogiskSlettDokument(journalpostDokumentInfoRelasjonList, requestTo);

		JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon = journalpostDokumentInfoRelasjonList.get(0);

		setDokumentLogiskSlettet(journalpostDokumentInfoRelasjon.getDokumentInfo());
		log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + " har utført logisk sletting av dokument med journalpostId={}, dokumentInfoId={}",
				requestTo.getJournalpostId(), requestTo.getDokumentInfoId());

		return LogiskSlettDokumentResponseMapper.mapToSlettDokumentResponse(journalpostDokumentInfoRelasjon.getJournalpost(),
				journalpostDokumentInfoRelasjon.getDokumentInfo());
	}

	private void setDokumentLogiskSlettet(DokumentInfo dokumentInfo) {
		dokumentInfo.setSlettet(true);
		dokumentInfo.setEndretAvNavn(MDC.get(MDCConstants.MDC_USER_NAME));
		dokumentInfo.setTittel(setSlettemelding(dokumentInfo.getTittel()));
		dokumentinfoRepository.save(dokumentInfo);
	}

	private String setSlettemelding(String tittel) {
		int minneAllokertForTittel = 500;
		String nyTittel = tittel;

		if (nyTittel.length() + SLETTEMELDING.length() <= minneAllokertForTittel) {
			nyTittel += SLETTEMELDING;
		}
		return nyTittel;
	}
}
