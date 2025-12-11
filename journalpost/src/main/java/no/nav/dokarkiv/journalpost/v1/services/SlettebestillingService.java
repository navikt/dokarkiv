package no.nav.dokarkiv.journalpost.v1.services;

import jakarta.transaction.Transactional;
import no.nav.dokarkiv.core.domain.codes.SlettebestillingArsakCode;
import no.nav.dokarkiv.core.domain.codes.SlettebestillingStatusCode;
import no.nav.dokarkiv.core.domain.entities.Slettebestilling;
import no.nav.dokarkiv.core.exceptions.UgyldigSlettebestillingException;
import no.nav.dokarkiv.core.exceptions.UnauthorizedForSlettebestillingException;
import no.nav.dokarkiv.core.repository.DokumentInfoRepository;
import no.nav.dokarkiv.core.repository.SakRepository;
import no.nav.dokarkiv.core.repository.SlettebestillingRepository;
import no.nav.dokarkiv.journalpost.v1.api.SlettebestillingRequest;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.domain.codes.SlettebestillingTypeCode.DOKUMENT;
import static no.nav.dokarkiv.core.domain.codes.SlettebestillingTypeCode.DOKUMENTER_PA_SAK;
import static no.nav.dokarkiv.core.domain.codes.SlettebestillingTypeCode.SAK;

@Service
public class SlettebestillingService {
	private static final String GOSYS_AUTHORIZED_PARTY_NAME = ":isa:gosys";
	private static final String TEAMDOKUMENTHANDTERING_AUTHORIZED_PARTY_NAME = ":teamdokumenthandtering:";

	private final SakRepository sakRepository;
	private final DokumentInfoRepository dokumentInfoRepository;
	private final SlettebestillingRepository slettebestillingRepository;
	private final Clock clock;

	public SlettebestillingService(SakRepository sakRepository, DokumentInfoRepository dokumentInfoRepository, SlettebestillingRepository slettebestillingRepository, Clock clock) {
		this.sakRepository = sakRepository;
		this.dokumentInfoRepository = dokumentInfoRepository;
		this.slettebestillingRepository = slettebestillingRepository;
		this.clock = clock;
	}

	@Transactional
	public Long bestillSletting(SlettebestillingRequest slettebestillingRequest, Optional<String> azpName) {
		validerSlettebestilling(slettebestillingRequest, azpName);
		Slettebestilling slettebestilling = mapSlettebestilling(slettebestillingRequest);
		return slettebestillingRepository.persist(slettebestilling).getId();
	}

	private void validerSlettebestilling(SlettebestillingRequest slettebestilling, Optional<String> azpName) {
		if (slettebestilling.slettebestillingType() == SAK || slettebestilling.slettebestillingType() == DOKUMENTER_PA_SAK) {
			if (azpName.filter(name -> name.contains(TEAMDOKUMENTHANDTERING_AUTHORIZED_PARTY_NAME)).isEmpty()) {
				throw new UnauthorizedForSlettebestillingException("Du har ikke tilgang til å opprette en slettebestilling med type " + slettebestilling.slettebestillingType());
			}
			if (slettebestilling.sakId() == null) {
				throw new UgyldigSlettebestillingException("SakId kan ikke være null når slettebestillingType er " + slettebestilling.slettebestillingType());
			}
			if (slettebestilling.arsak() != SlettebestillingArsakCode.BEVARINGSTID) {
				throw new UgyldigSlettebestillingException("Årsak må være BEVARINGSTID når slettebestillingType er " + slettebestilling.slettebestillingType());
			}
			if (!sakRepository.existsById(slettebestilling.sakId())) {
				throw new UgyldigSlettebestillingException("Kunne ikke bestille sletting av sak med Id " + slettebestilling.sakId() + " fordi den ikke ble funnet.");
			}
		} else if (slettebestilling.slettebestillingType() == DOKUMENT) {
			if (azpName.filter(name -> name.contains(GOSYS_AUTHORIZED_PARTY_NAME)).isEmpty()) {
				throw new UnauthorizedForSlettebestillingException("Du har ikke tilgang til å opprette en slettebestilling med type DOKUMENT");
			}
			if (slettebestilling.dokumentInfoId() == null) {
				throw new UgyldigSlettebestillingException("DokumentInfoId kan ikke være null når slettebestillingType er DOKUMENT");
			}
			if (slettebestilling.arsak() != SlettebestillingArsakCode.ENKELTSLETTING) {
				throw new UgyldigSlettebestillingException("Årsak må være ENKELTSLETTING når slettebestillingType er DOKUMENT");
			}
			if (!dokumentInfoRepository.existsById(slettebestilling.dokumentInfoId())) {
				throw new UgyldigSlettebestillingException("Kunne ikke bestille sletting av dokument med Id " + slettebestilling.dokumentInfoId() + " fordi det ikke ble funnet.");
			}
		} else {
			throw new UgyldigSlettebestillingException(slettebestilling.slettebestillingType() + " er ikke en gyldig verdi for slettebestillingType");
		}
	}

	private Slettebestilling mapSlettebestilling(SlettebestillingRequest request) {
		Slettebestilling slettebestilling = Slettebestilling.builder()
				.begrunnelse(request.begrunnelse())
				.dokumentInfoId(request.dokumentInfoId())
				.sakId(request.sakId())
				.slettebestillingType(request.slettebestillingType())
				.slettebestillingArsak(request.arsak())
				.slettebestillingHjemmel(request.hjemmel())
				.slettebestillingStatus(SlettebestillingStatusCode.OPPRETTET)
				.datoUtfores(determineDatoUtfores(request))
				.build();
		slettebestilling.setOpprettetKildeNavn(MDC.get(MDC_CONSUMER_ID));
		return slettebestilling;
	}

	private LocalDate determineDatoUtfores(SlettebestillingRequest request) {
		return switch (request.slettebestillingType()) {
			case DOKUMENT -> LocalDate.now(clock).plusDays(21);
			case SAK, DOKUMENTER_PA_SAK -> LocalDate.now(clock).plusDays(365);
		};
	}
}
