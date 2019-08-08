package no.nav.dokarkiv.journalpost.v1.services;

import static java.lang.Long.parseLong;
import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.aksjonslogg.LagreAksjonsLoggService;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.UgyldigJournalStatusException;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;

@Component
@Slf4j
public class AvbrytService {
    private final JoarkRepository joarkRepository;
    private final LagreAksjonsLoggService aksjonsLoggService;

    private static final String FIKK_AVBRUTT_UTGAAR = "Journalposten ble satt til avbrutt / utgår";

    @Inject
    public AvbrytService(final JoarkRepository joarkRepository,
                         final LagreAksjonsLoggService aksjonsLoggService
    ) {
        this.joarkRepository = joarkRepository;
        this.aksjonsLoggService = aksjonsLoggService;
    }

    public String avbryt(String journalpostId) {
        Journalpost journalpost = joarkRepository.findById(parseLong(journalpostId))
                .orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", journalpostId)));

        JournalStatusCode oldJournalStatus = journalpost.getJournalstatus();
        if (Arrays.asList(JournalStatusCode.OD, JournalStatusCode.M, JournalStatusCode.MO, JournalStatusCode.UB)
                .contains(oldJournalStatus)) {
            journalpost.setJournalstatus(JournalStatusCode.U);
        } else if (Arrays.asList(JournalStatusCode.D, JournalStatusCode.R).contains(oldJournalStatus)) {
            journalpost.setJournalstatus(JournalStatusCode.A);
        } else if (Arrays.asList(JournalStatusCode.A, JournalStatusCode.U).contains(oldJournalStatus)) {
            throw new UgyldigJournalStatusException("Journalposten er allerede avbrutt)");
        } else {
            throw new UgyldigJournalStatusException("Journalposten kan ikke avbrytes da den er ferdigstilt)");
        }

        ArkivElementEndringTO endring = ArkivElementEndringTO.builder()
                .arkivElement("Journalpost.journalStatus")
                .fraVerdi(oldJournalStatus.name())
                .tilVerdi(journalpost.getJournalstatus().name())
                .build();

        joarkRepository.save(journalpost);

        aksjonsLoggService.lagreAksjonsLoggForJournalpost(
                AksjonsTypeCode.AVBRYT, journalpost.getJournalpostId(), "ARKL", FIKK_AVBRUTT_UTGAAR,
                null, Collections.singletonList(endring));

        log.info(MDC.get(MDC_REQUEST_ID) + " har satt status til avbrutt / utgår for journalpost med journalpostId={}", journalpostId);

        return FIKK_AVBRUTT_UTGAAR;
    }
}