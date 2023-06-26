package no.nav.dokarkiv.sikkerhetsnivaa;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.repository.SikkerhetsnivaaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static java.lang.String.format;

@Slf4j
@Service
public class FinnIkkeLesteJournalposterService {

	private final SikkerhetsnivaaRepository sikkerhetsnivaaRepository;

	public FinnIkkeLesteJournalposterService(SikkerhetsnivaaRepository sikkerhetsnivaaRepository) {
		this.sikkerhetsnivaaRepository = sikkerhetsnivaaRepository;
	}

	public List<Long> finnIkkeLesteJournalposter(String utsendingskanal, LocalDateTime ekspedertFra, LocalDateTime ekspedertTil) {
		validateInput(ekspedertFra, ekspedertTil);
		Date datoOpprettetStart = convertToDate(ekspedertFra.minusDays(30));
		Date datoOpprettetSlutt = convertToDate(ekspedertTil.plusDays(2));
		return sikkerhetsnivaaRepository.findIkkeLesteJournalposts(UtsendingsKanalCode.fromString(utsendingskanal), convertToDate(ekspedertFra), convertToDate(ekspedertTil), datoOpprettetStart, datoOpprettetSlutt);
	}

	private void validateInput(LocalDateTime ekspedertFra, LocalDateTime ekspedertTil) {
		if (ekspedertTil.isBefore(ekspedertFra)) {
			throw new InputValideringFeiletException(
					format("EkspedertFra kan ikke være før ekspedertTil. ekspedertFra=%s, ekspedertTil=%s", ekspedertFra, ekspedertTil));
		}
	}

	public Date convertToDate(LocalDateTime dateToConvert) {
		return java.sql.Timestamp.valueOf(dateToConvert);
	}
}


