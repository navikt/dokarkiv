package no.nav.dokarkiv.journalpost.v1.services;

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
public class FeilregistrerSaksrelasjonService {
    private final JoarkRepository joarkRepository;

    @Inject
    public FeilregistrerSaksrelasjonService(final JoarkRepository joarkRepository) {
        this.joarkRepository = joarkRepository;
    }

    public List<ArkivElementEndringTO> feilregistrerSaksrelasjon(String journalpostId) throws UgyldigInputException {
        Journalpost journalpost = joarkRepository.findById(parseLong(journalpostId))
                .orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", journalpostId)));

        Saksrelasjon saksrelasjon = journalpost.getSaksrelasjon();

        if (saksrelasjon.getFeilregistrert() == null || !saksrelasjon.getFeilregistrert()) {
            journalpost.getSaksrelasjon().setFeilregistrert(true);
        } else if (saksrelasjon.getFeilregistrert()) {
            throw new UgyldigInputException("Saksrelasjonen er allerede feilregistrert");
        } else {
            throw new UgyldigInputException("Feilregistrering er ikke mulig fordi journalposten ikke er knyttet til sak");
        }

        joarkRepository.save(journalpost);

        return Arrays.asList(ArkivElementEndringTO.builder()
                .arkivElement("Journalpost.Saksrelasjon.feilregistrert")
                .fraVerdi("false")
                .tilVerdi("true")
                .build());
    }
}