package no.nav.dokarkiv.journalpost.v1.services;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.aksjonslogg.LagreAksjonsLoggService;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.UgyldigJournalStatusException;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Collections;

import static java.lang.Long.parseLong;
import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.AVBRYT;
import static no.nav.dokarkiv.journalpost.v1.util.JournalStatusCodeConstants.AVBRUT_JOURNAL_STATUS_CODE;
import static no.nav.dokarkiv.journalpost.v1.util.JournalStatusCodeConstants.INNGÅENDE_JOURNAL_STATUS_CODE;
import static no.nav.dokarkiv.journalpost.v1.util.JournalStatusCodeConstants.UTGÅENDE_OR_NOTAT_JOURNAL_STATUS_CODE;

@Component
@Slf4j
public class AvbrytService {
    private final JoarkRepository joarkRepository;
    private final LagreAksjonsLoggService aksjonsLoggService;

    static final String FIKK_AVBRUTT = "Journalposten ble satt til avbrutt";

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
        if (INNGÅENDE_JOURNAL_STATUS_CODE.contains(oldJournalStatus)) {
            throw new UgyldigJournalStatusException("Journalposten er inngående. Kun utgående journalposter og notater kan avbrytes");
        } else if (UTGÅENDE_OR_NOTAT_JOURNAL_STATUS_CODE.contains(oldJournalStatus)) {
            journalpost.setJournalstatus(JournalStatusCode.A);
        } else if (AVBRUT_JOURNAL_STATUS_CODE.contains(oldJournalStatus)) {
            throw new UgyldigJournalStatusException("Journalposten er allerede avbrutt");
        } else {
            throw new UgyldigJournalStatusException("Journalposten kan ikke avbrytes da den er ferdigstilt");
        }

        ArkivElementEndringTO endring = ArkivElementEndringTO.builder()
                .arkivElement("Journalpost.journalStatus")
                .fraVerdi(oldJournalStatus.name())
                .tilVerdi(journalpost.getJournalstatus().name())
                .build();

        joarkRepository.save(journalpost);

        aksjonsLoggService.lagreAksjonsLoggForJournalpost(
                AVBRYT, journalpost.getJournalpostId(), "ARKL", FIKK_AVBRUTT,
                null, Collections.singletonList(endring));

        log.info(MDC.get(MDC_REQUEST_ID) + " har satt status til avbrutt for journalpost med journalpostId={}", journalpostId);

        return FIKK_AVBRUTT;
    }
}