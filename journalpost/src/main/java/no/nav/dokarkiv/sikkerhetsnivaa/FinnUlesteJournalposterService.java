package no.nav.dokarkiv.sikkerhetsnivaa;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.repository.SikkerhetsnivaaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static java.lang.String.format;

@Slf4j
@Service
public class FinnUlesteJournalposterService {

	private final SikkerhetsnivaaRepository sikkerhetsnivaaRepository;

	public FinnUlesteJournalposterService(SikkerhetsnivaaRepository sikkerhetsnivaaRepository) {
		this.sikkerhetsnivaaRepository = sikkerhetsnivaaRepository;
	}

	public List<Long> finnUlesteJournalposter(String utsendingskanal, LocalDateTime ekspedertFra, LocalDateTime ekspedertTil) {
		validateInput(ekspedertFra, ekspedertTil);
		LocalDateTime datoOpprettetStart = ekspedertFra.minusDays(90);
		LocalDateTime datoOpprettetSlutt = ekspedertTil.plusDays(2);
		return sikkerhetsnivaaRepository.finnUlesteJournalposter(UtsendingsKanalCode.fromString(utsendingskanal), ekspedertFra, ekspedertTil, datoOpprettetStart, datoOpprettetSlutt);
	}

	private void validateInput(LocalDateTime ekspedertFra, LocalDateTime ekspedertTil) {
		if (ekspedertTil.isBefore(ekspedertFra)) {
			throw new InputValideringFeiletException(
					format("EkspedertFra kan ikke være før ekspedertTil. ekspedertFra=%s, ekspedertTil=%s", ekspedertFra, ekspedertTil));
		}
	}
}


