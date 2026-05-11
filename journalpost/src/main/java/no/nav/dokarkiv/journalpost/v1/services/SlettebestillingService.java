package no.nav.dokarkiv.journalpost.v1.services;

import jakarta.transaction.Transactional;
import no.nav.dokarkiv.core.domain.codes.SlettebestillingArsakCode;
import no.nav.dokarkiv.core.domain.codes.SlettebestillingHjemmelCode;
import no.nav.dokarkiv.core.domain.codes.SlettebestillingStatusCode;
import no.nav.dokarkiv.core.domain.codes.SlettebestillingTypeCode;
import no.nav.dokarkiv.core.domain.entities.Slettebestilling;
import no.nav.dokarkiv.core.exceptions.DokumentInfoIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.UgyldigSlettebestillingException;
import no.nav.dokarkiv.core.repository.DokumentInfoRepository;
import no.nav.dokarkiv.core.repository.SlettebestillingRepository;
import no.nav.dokarkiv.journalpost.v1.api.SlettebestillingRequest;
import no.nav.dokarkiv.journalpost.v1.validators.SlettebestillingValidator;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_NAME;

@Service
public class SlettebestillingService {
	private static final int DOKUMENT_SLETTING_VENTETID_DAGER = 21;

	private final DokumentInfoRepository dokumentInfoRepository;
	private final SlettebestillingRepository slettebestillingRepository;
	private final Clock clock;

	public SlettebestillingService(DokumentInfoRepository dokumentInfoRepository, SlettebestillingRepository slettebestillingRepository, Clock clock) {
		this.dokumentInfoRepository = dokumentInfoRepository;
		this.slettebestillingRepository = slettebestillingRepository;
		this.clock = clock;
	}

	@Transactional
	public long bestillSletting(long dokumentInfoId, SlettebestillingRequest slettebestillingRequest) {
		validerSlettebestilling(dokumentInfoId, slettebestillingRequest);
		Slettebestilling slettebestilling = mapSlettebestilling(dokumentInfoId, slettebestillingRequest);
		return slettebestillingRepository.persist(slettebestilling).getId();
	}

	@Transactional
	public void opphevBestillSletting(long dokumentInfoId) {
		List<Slettebestilling> slettebestillinger = slettebestillingRepository.findByDokumentInfoId(dokumentInfoId);

		if (slettebestillinger.stream().anyMatch(slettebestilling -> slettebestilling.getSlettebestillingStatus() == SlettebestillingStatusCode.FERDIGSTILT)) {
			throw new UgyldigSlettebestillingException("Kan ikke oppheve sletting som allerede er gjennomført!");
		}

		slettebestillinger
			.stream()
			.filter(slettebestilling -> slettebestilling.getSlettebestillingStatus() == SlettebestillingStatusCode.OPPRETTET)
			.forEach(this::avbrytSletting);
	}

	private void avbrytSletting(Slettebestilling slettebestilling) {
		slettebestilling.endreSlettebestillingStatus(SlettebestillingStatusCode.AVBRUTT, MDC.get(MDC_CONSUMER_ID));
	}

	private void validerSlettebestilling(long dokumentInfoId, SlettebestillingRequest slettebestilling) {
		SlettebestillingValidator.validerSlettebestilling(slettebestilling);
		if (!dokumentInfoRepository.existsById(dokumentInfoId)) {
			throw new DokumentInfoIkkeFunnetException("Kunne ikke bestille sletting av dokument med dokumentInfoId=%d fordi det ikke ble funnet.".formatted(dokumentInfoId));
		}
	}

	private Slettebestilling mapSlettebestilling(long dokumentInfoId, SlettebestillingRequest request) {
		Slettebestilling slettebestilling = Slettebestilling.builder()
				.slettebestillingType(SlettebestillingTypeCode.DOKUMENT)
				.dokumentInfoId(dokumentInfoId)
				.slettebestillingStatus(SlettebestillingStatusCode.OPPRETTET)
				.slettebestillingHjemmel(SlettebestillingHjemmelCode.valueOf(request.hjemmel()))
				.slettebestillingArsak(SlettebestillingArsakCode.ENKELTSLETTING)
				.begrunnelse(request.begrunnelse())
				.datoUtfores(determineDatoUtforesForBestillingstypeDokument())
				.opprettetAvNavn(MDC.get(MDC_USER_NAME))
				.endretAvNavn(MDC.get(MDC_USER_NAME))
				.build();
		slettebestilling.setOpprettetAvOgChangestamp(MDC.get(MDC_USER_ID));
		slettebestilling.setOpprettetKildeNavn(MDC.get(MDC_CONSUMER_ID));
		slettebestilling.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
		return slettebestilling;
	}

	private LocalDate determineDatoUtforesForBestillingstypeDokument() {
		return LocalDate.now(clock).plusDays(DOKUMENT_SLETTING_VENTETID_DAGER);
	}
}
