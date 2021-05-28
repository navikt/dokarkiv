package no.nav.dokarkiv.journalpost.v1.services;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.aksjonslogg.LagreAksjonsLoggService;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.UgyldigJournalStatusException;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.lang.Long.parseLong;
import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.UTGAAR;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.A;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.D;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.R;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.U;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.I;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.N;

@Component
@Slf4j
public class UtgaarService {
	private final JoarkRepository joarkRepository;
	private final LagreAksjonsLoggService aksjonsLoggService;

	static final String FIKK_UTGAAR = "Journalposten ble satt til utgår";
	static final List<JournalStatusCode> JOURNAL_STATUS_AVBRUTT_DOKUMENT_RESERVERT = Arrays.asList(A, D, R);
	static final List<JournalpostTypeCode> JOURNALPOSTTYPE_INNGAAENDE_NOTAT = Arrays.asList(I, N);

	@Inject
	public UtgaarService(final JoarkRepository joarkRepository, final LagreAksjonsLoggService aksjonsLoggService) {
		this.joarkRepository = joarkRepository;
		this.aksjonsLoggService = aksjonsLoggService;
	}

	public String settStatusUtgaar(String journalpostId) {
		Journalpost journalpost = joarkRepository.findById(parseLong(journalpostId))
				.orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", journalpostId)));

		JournalStatusCode oldJournalStatus = journalpost.getJournalstatus();
		JournalpostTypeCode journalposttype = journalpost.getJournalposttype();

		if (JOURNALPOSTTYPE_INNGAAENDE_NOTAT.contains(journalposttype)) {
			journalpost.setJournalstatus(U);
		} else if (JOURNAL_STATUS_AVBRUTT_DOKUMENT_RESERVERT.contains(oldJournalStatus)) {
			throw new UgyldigJournalStatusException("Journalposten er utgående eller notat. Kun inngående journalposter kan settes til Utgår");
		} else if (U.equals(oldJournalStatus)) {
			throw new UgyldigJournalStatusException("Journalposten er allerede satt til Utgår");
		} else {
			throw new UgyldigJournalStatusException("Journalposten kan ikke settes til utgår, da den er ferdigstilt");
		}

		ArkivElementEndringTO endring = ArkivElementEndringTO.builder()
				.arkivElement("Journalpost.journalStatus")
				.fraVerdi(oldJournalStatus.name())
				.tilVerdi(journalpost.getJournalstatus().name())
				.build();

		joarkRepository.save(journalpost);

		aksjonsLoggService.lagreAksjonsLoggForJournalpost(
				UTGAAR,
				journalpost.getJournalpostId(),
				"ARKL",
				FIKK_UTGAAR,
				null,
				Collections.singletonList(endring)
		);

		log.info(MDC.get(MDC_REQUEST_ID) + " har satt status til utgår for journalpost med journalpostId={}", journalpostId);

		return FIKK_UTGAAR;
	}
}