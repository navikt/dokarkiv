package no.nav.dokarkiv.journalpost.v1.services;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.repository.JournalpostRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static java.lang.String.format;

@Slf4j
@Service
public class FinnIkkeLesteJournalposterService {

	private final JournalpostRepository journalpostRepository;

	public FinnIkkeLesteJournalposterService(JournalpostRepository journalpostRepository) {
		this.journalpostRepository = journalpostRepository;
	}

	public List<Long> finnIkkeLesteJournalposter(String utsendingskanal, LocalDateTime ekspedertFra, LocalDateTime ekspedertTil) {
		validateInput(utsendingskanal, ekspedertFra, ekspedertTil);
		return journalpostRepository.findIkkeLesteJournalposts(UtsendingsKanalCode.valueOf(utsendingskanal), convertToDate(ekspedertFra), convertToDate(ekspedertTil));
	}

	private void validateInput(String utsendingskanal, LocalDateTime ekspedertFra, LocalDateTime ekspedertTil) {
		if (utsendingskanal == null || ekspedertFra == null || ekspedertTil == null) {
			throw new InputValideringFeiletException(
					format("finnIkkeLesteJournalposter feilet med ugyldig input. utsendingskanal:%s, ekspedertFra:%s, ekspedertTil:%s", utsendingskanal, ekspedertFra, ekspedertTil));
		}
		try {
			UtsendingsKanalCode.valueOf(utsendingskanal);
		} catch (IllegalArgumentException e) {
			throw new InputValideringFeiletException(
					format("utsendingskanal er ikke en gyldig utsendingskanal! Input utsendingskanal:%s", utsendingskanal));
		}
		if (ekspedertTil.isBefore(ekspedertFra)) {
			throw new InputValideringFeiletException(
					format("EkspedertFra kan ikke være før ekspedertTil. ekspedertFra:%s, ekspedertTil:%s", ekspedertFra, ekspedertTil));
		}
	}

	public Date convertToDate(LocalDateTime dateToConvert) {
		return java.sql.Timestamp.valueOf(dateToConvert);
	}
}


