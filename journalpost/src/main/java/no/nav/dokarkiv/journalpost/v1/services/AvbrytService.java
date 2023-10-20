package no.nav.dokarkiv.journalpost.v1.services;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.aksjonslogg.LagreAksjonsLoggService;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.UgyldigJournalStatusException;
import no.nav.dokarkiv.core.repository.JournalpostRepository;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.lang.Long.parseLong;
import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.JOURNALPOST_JOURNALSTATUS;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.AVBRYT;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.A;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.D;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.R;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.I;

@Component
@Slf4j
public class AvbrytService {
    private final JournalpostRepository journalpostRepository;
    private final LagreAksjonsLoggService aksjonsLoggService;

    static final String FIKK_AVBRUTT = "Journalposten ble satt til avbrutt";
    static final List<JournalStatusCode> JOURNAL_STATUS_CODE_DOKUMENT_RESERVERT = Arrays.asList(D, R);

    public AvbrytService(final JournalpostRepository journalpostRepository,
                         final LagreAksjonsLoggService aksjonsLoggService
    ) {
        this.journalpostRepository = journalpostRepository;
        this.aksjonsLoggService = aksjonsLoggService;
    }

    public String avbryt(String journalpostId) {
        Journalpost journalpost = journalpostRepository.findById(parseLong(journalpostId))
                .orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", journalpostId)));

        JournalStatusCode oldJournalStatus = journalpost.getJournalstatus();
        JournalpostTypeCode journalposttype = journalpost.getJournalposttype();

        if (I.equals(journalposttype)) {
            throw new UgyldigJournalStatusException("Journalposten er inngående. Kun utgående journalposter og notater kan avbrytes, med journalpostId " + journalpostId);
        } else if (JOURNAL_STATUS_CODE_DOKUMENT_RESERVERT.contains(oldJournalStatus)) {
            journalpost.setJournalstatus(A);
        } else if (A.equals(oldJournalStatus)) {
            throw new UgyldigJournalStatusException("Journalposten er allerede avbrutt, med journalpostId " + journalpostId);
        } else {
            throw new UgyldigJournalStatusException("Journalposten kan ikke avbrytes da den er ferdigstilt, med journalpostId " + journalpostId);
        }

        ArkivElementEndringTO endring = ArkivElementEndringTO.builder()
                .arkivElement(JOURNALPOST_JOURNALSTATUS)
                .fraVerdi(oldJournalStatus.name())
                .tilVerdi(journalpost.getJournalstatus().name())
                .build();

        aksjonsLoggService.lagreAksjonsLoggForJournalpost(
                AVBRYT, journalpost.getJournalpostId(), "ARKL", FIKK_AVBRUTT,
                null, Collections.singletonList(endring));

        log.info(MDC.get(MDC_REQUEST_ID) + " har satt status til avbrutt for journalpost med journalpostId={}", journalpostId);

        return FIKK_AVBRUTT;
    }
}