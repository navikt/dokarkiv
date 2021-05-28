package no.nav.dokarkiv.journalpost.v1.services;

import static java.lang.Long.parseLong;

import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.UgyldigJournalStatusException;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

@Component
public class UkjentBrukerService {
    private final JoarkRepository joarkRepository;
    private static final List<JournalStatusCode> validJournalStatusList = Arrays.asList(JournalStatusCode.U, JournalStatusCode.OD, JournalStatusCode.M, JournalStatusCode.MO);

    @Inject
    public UkjentBrukerService(final JoarkRepository joarkRepository) {
        this.joarkRepository = joarkRepository;
    }

    public List<ArkivElementEndringTO> settUkjentBruker(String journalpostId) {
        Journalpost journalpost = joarkRepository.findById(parseLong(journalpostId))
                .orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", journalpostId)));

        JournalStatusCode oldJournalStatus = journalpost.getJournalstatus();
        if (validJournalStatusList.contains(oldJournalStatus)) {
            journalpost.setJournalstatus(JournalStatusCode.UB);
        } else {
            throw new UgyldigJournalStatusException("Journalpost kan ikke settes til UB (ukjent bruker)");
        }

        joarkRepository.save(journalpost);

        return Arrays.asList(ArkivElementEndringTO.builder()
                .arkivElement("Journalpost.journalStatus")
                .fraVerdi(oldJournalStatus.name())
                .tilVerdi(journalpost.getJournalstatus().name())
                .build());
    }
}
