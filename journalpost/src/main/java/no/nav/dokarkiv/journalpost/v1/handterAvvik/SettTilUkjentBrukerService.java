package no.nav.dokarkiv.journalpost.v1.handterAvvik;

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
public class SettTilUkjentBrukerService {
    private final JoarkRepository joarkRepository;

    @Inject
    public SettTilUkjentBrukerService(final JoarkRepository joarkRepository) {
        this.joarkRepository = joarkRepository;
    }

    public List<ArkivElementEndringTO> settTilUkjentBruker(String journalpostId) throws UgyldigInputException {
        Journalpost journalpost = joarkRepository.findById(parseLong(journalpostId))
                .orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", journalpostId)));

        if (Arrays.asList(JournalStatusCode.U, JournalStatusCode.OD, JournalStatusCode.M, JournalStatusCode.MO)
                .contains(journalpost.getJournalstatus())) {
            journalpost.setJournalstatus(JournalStatusCode.UB);
        } else {
            throw new UgyldigInputException("Journalpost kan ikke settes til UB (ukjent bruker)");
        }

        joarkRepository.save(journalpost);

        return Arrays.asList(ArkivElementEndringTO.builder()
                .arkivElement("Journalpost.Saksrelasjon.feilregistrert")
                .fraVerdi("true")
                .tilVerdi("false")
                .build());
    }
}
