package no.nav.dokarkiv.logiskslettdokument.rjoark101;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import no.nav.dokarkiv.logiskslettdokument.rjoark100.LogiskSlettDokumentRequestTo;
import no.nav.dokarkiv.logiskslettdokument.rjoark100.LogiskSlettDokumentResponse;
import no.nav.dokarkiv.logiskslettdokument.rjoark100.LogiskSlettDokumentResponseMapper;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class AngreLogiskSlettDokumentService {

	@Value("${logiskslettdokument.slettemelding}")
	private static String SLETTEMELDING;

	private final AngreLogiskSlettDokumentValidator validator;
	private final DokumentinfoRepository dokumentinfoRepository;
	private final JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;

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

		validator.validateAngreLogiskSlettDokument(journalpostDokumentInfoRelasjonList, requestTo);

		JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon = journalpostDokumentInfoRelasjonList.get(0);

		setAngreDokumentLogiskSlettet(journalpostDokumentInfoRelasjon.getDokumentInfo());
		log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + " har angret logisk sletting av dokument med journalpostId={}, dokumentInfoId={}",
				requestTo.getJournalpostId(), requestTo.getDokumentInfoId());

		return LogiskSlettDokumentResponseMapper.mapToSlettDokumentResponse(journalpostDokumentInfoRelasjon.getJournalpost(),
				journalpostDokumentInfoRelasjon.getDokumentInfo());
	}

	private void setAngreDokumentLogiskSlettet(DokumentInfo dokumentInfo) {
		dokumentInfo.setSlettet(false);
		dokumentInfo.setEndretAvNavn(MDC.get(MDCConstants.MDC_USER_NAME));
		dokumentInfo.setTittel(fjernSlettemelding(dokumentInfo.getTittel()));
		dokumentinfoRepository.save(dokumentInfo);
	}

	private String fjernSlettemelding(String tittel) {
		String nyTittel = tittel;

		if (tittel.endsWith(SLETTEMELDING)) {
			nyTittel = tittel.substring(0, tittel.length() - SLETTEMELDING.length());
		}
		return nyTittel;
	}

}
