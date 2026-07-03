package no.nav.dokarkiv.internal.finnulestejournalposter;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.repository.FinnUlesteJournalposterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static java.lang.String.format;

@Slf4j
@Service
@Transactional(readOnly = true)
public class FinnUlesteJournalposterService {

	private final FinnUlesteJournalposterRepository finnUlesteJournalposterRepository;

	public FinnUlesteJournalposterService(FinnUlesteJournalposterRepository finnUlesteJournalposterRepository) {
		this.finnUlesteJournalposterRepository = finnUlesteJournalposterRepository;
	}

	public List<Long> finnUlesteJournalposter(String utsendingskanal, LocalDateTime ekspedertFra, LocalDateTime ekspedertTil) {
		validateInput(ekspedertFra, ekspedertTil);
		LocalDateTime datoOpprettetStart = ekspedertFra.minusDays(90);
		LocalDateTime datoOpprettetSlutt = ekspedertTil.plusDays(2);
		return finnUlesteJournalposterRepository.finnUlesteJournalposter(UtsendingsKanalCode.fromString(utsendingskanal), ekspedertFra, ekspedertTil, datoOpprettetStart, datoOpprettetSlutt);
	}

	private void validateInput(LocalDateTime ekspedertFra, LocalDateTime ekspedertTil) {
		if (ekspedertTil.isBefore(ekspedertFra)) {
			throw new InputValideringFeiletException(
					format("EkspedertFra kan ikke være før ekspedertTil. ekspedertFra=%s, ekspedertTil=%s", ekspedertFra, ekspedertTil));
		}
	}
}
