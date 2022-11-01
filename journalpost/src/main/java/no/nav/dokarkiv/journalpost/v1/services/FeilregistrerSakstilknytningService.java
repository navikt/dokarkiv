package no.nav.dokarkiv.journalpost.v1.services;

import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.exceptions.FeilregistreringAlleredeOpphevetException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeKnyttetTilSakException;
import no.nav.dokarkiv.core.exceptions.SaksrelasjonAlleredeFeilregistrertException;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.repository.SaksrelasjonRepository;
import org.hibernate.StaleObjectStateException;
import org.slf4j.MDC;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import static java.lang.Long.parseLong;
import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_NAME;
import static no.nav.dokarkiv.journalpost.v1.JournalpostApiConfig.RETRY_DELAY;
import static no.nav.dokarkiv.journalpost.v1.JournalpostApiConfig.RETRY_MULTIPLIER;

@Component
public class FeilregistrerSakstilknytningService {
    private final JoarkRepository joarkRepository;
    private final SaksrelasjonRepository saksrelasjonRepository;

    public FeilregistrerSakstilknytningService(final JoarkRepository joarkRepository,
                                               final SaksrelasjonRepository saksrelasjonRepository) {
        this.joarkRepository = joarkRepository;
        this.saksrelasjonRepository = saksrelasjonRepository;
    }

    @Retryable(
            include = {ObjectOptimisticLockingFailureException.class, StaleObjectStateException.class},
            backoff = @Backoff(delay = RETRY_DELAY, multiplier = RETRY_MULTIPLIER)
    )
    public List<ArkivElementEndringTO> feilregistrerSakstilknytning(String journalpostId) {
        Saksrelasjon saksrelasjon = hentSaksRelasjonForJournalpost(journalpostId);

        if (saksrelasjon.getFeilregistrert() == null || !saksrelasjon.getFeilregistrert()) {
            saksrelasjon.setFeilregistrert(true);
            saksrelasjon.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
            saksrelasjon.setEndretAvNavn(MDC.get(MDC_USER_NAME));
        } else {
            throw new SaksrelasjonAlleredeFeilregistrertException("Saksrelasjonen er allerede feilregistrert");
        }

        saksrelasjonRepository.save(saksrelasjon);

        return Collections.singletonList(ArkivElementEndringTO.builder()
                .arkivElement("Journalpost.Saksrelasjon.feilregistrert")
                .fraVerdi("false")
                .tilVerdi("true")
                .build());
    }

    public List<ArkivElementEndringTO> opphevFeilregistrertSakstilknytning(String journalpostId) {
        Saksrelasjon saksrelasjon = hentSaksRelasjonForJournalpost(journalpostId);

        if (saksrelasjon.getFeilregistrert() == null || !saksrelasjon.getFeilregistrert()) {
            throw new FeilregistreringAlleredeOpphevetException("Feilregistreringen er allerede opphevet");
        } else {
            saksrelasjon.setFeilregistrert(false);
        }

        saksrelasjonRepository.save(saksrelasjon);

        return Collections.singletonList(ArkivElementEndringTO.builder()
                .arkivElement("Journalpost.Saksrelasjon.feilregistrert")
                .fraVerdi("true")
                .tilVerdi("false")
                .build());
    }

    private Saksrelasjon hentSaksRelasjonForJournalpost(String journalpostId) {
        assertJournalpostExists(journalpostId);
        return saksrelasjonRepository.findSaksrelasjonByJournalpostId(journalpostId)
                .orElseThrow(() -> new JournalpostIkkeKnyttetTilSakException("Feilregistrering er ikke mulig fordi journalposten ikke er knyttet til sak"));
    }

    private void assertJournalpostExists(String journalpostId) {
        if (!joarkRepository.existsById(parseLong(journalpostId))) {
            throw new JournalpostIkkeFunnetException(String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", journalpostId));
        }
    }

}