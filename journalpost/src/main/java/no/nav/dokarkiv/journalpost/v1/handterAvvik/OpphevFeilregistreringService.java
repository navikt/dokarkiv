package no.nav.dokarkiv.journalpost.v1.handterAvvik;

import static java.lang.Long.parseLong;

import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.UgyldigInputException;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

@Component
public class OpphevFeilregistreringService {
    private final JoarkRepository joarkRepository;

    @Inject
    public OpphevFeilregistreringService(final JoarkRepository joarkRepository) {
        this.joarkRepository = joarkRepository;
    }

    public List<ArkivElementEndringTO> opphevFeilregistrering(String journalpostId) throws UgyldigInputException {
        Journalpost journalpost = joarkRepository.findById(parseLong(journalpostId))
                .orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", journalpostId)));

        Saksrelasjon saksrelasjon = journalpost.getSaksrelasjon();

        if (!saksrelasjon.getFeilregistrert()) {
            throw new UgyldigInputException("Feilregistreringen er allerede opphevet");
        } else if (saksrelasjon.getFeilregistrert()) {
            journalpost.getSaksrelasjon().setFeilregistrert(false);
        } else {
            throw new UgyldigInputException("Feilregistrering er ikke mulig fordi journalposten ikke er knyttet til sak");
        }

        joarkRepository.save(journalpost);

        return Arrays.asList(ArkivElementEndringTO.builder()
                .arkivElement("Journalpost.Saksrelasjon.feilregistrert")
                .fraVerdi("true")
                .tilVerdi("false")
                .build());
    }
}