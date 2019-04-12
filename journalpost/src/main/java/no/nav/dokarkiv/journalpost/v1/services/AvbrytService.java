package no.nav.dokarkiv.journalpost.v1.services;

import static java.lang.Long.parseLong;

import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.UgyldigInputException;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

@Component
public class AvbrytService {
    private final JoarkRepository joarkRepository;

    @Inject
    public AvbrytService(final JoarkRepository joarkRepository) {
        this.joarkRepository = joarkRepository;
    }

    public List<ArkivElementEndringTO> avbryt(String journalpostId) throws UgyldigInputException {
        Journalpost journalpost = joarkRepository.findById(parseLong(journalpostId))
                .orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", journalpostId)));

        if (Arrays.asList(JournalStatusCode.OD, JournalStatusCode.M, JournalStatusCode.MO, JournalStatusCode.UB)
                .contains(journalpost.getJournalstatus())) {
            journalpost.setJournalstatus(JournalStatusCode.U);
        } else if (Arrays.asList(JournalStatusCode.D, JournalStatusCode.R).contains(journalpost.getJournalstatus())) {
            journalpost.setJournalstatus(JournalStatusCode.A);
        } else if (Arrays.asList(JournalStatusCode.A, JournalStatusCode.U).contains(journalpost.getJournalstatus())) {
            throw new UgyldigInputException("Journalposten er allerede avbrutt)");
        } else {
            throw new UgyldigInputException("Journalposten kan ikke avbrytes da den er ferdigstilt)");
        }

        joarkRepository.save(journalpost);

        return Arrays.asList(ArkivElementEndringTO.builder()
                .arkivElement("Journalpost.Saksrelasjon.feilregistrert")
                .fraVerdi("true")
                .tilVerdi("false")
                .build());
    }
}