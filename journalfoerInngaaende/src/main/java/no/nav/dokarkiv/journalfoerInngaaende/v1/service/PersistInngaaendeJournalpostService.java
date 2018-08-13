package no.nav.dokarkiv.journalfoerInngaaende.v1.service;

import static org.hibernate.annotations.common.util.StringHelper.isEmpty;

import no.nav.dokarkiv.core.consumer.aktoer.DefaultAktoerConsumerService;
import no.nav.dokarkiv.core.consumer.aktoer.to.HentAktoerIdForIdentRequestTo;
import no.nav.dokarkiv.core.consumer.aktoer.to.HentAktoerIdForIdentResponseTo;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.DokarkivRestFunctionalException;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.journalfoerInngaaende.v1.to.AktoerTo;
import no.nav.dokarkiv.journalfoerInngaaende.v1.to.JournalpostResponseTo;
import no.nav.dokarkiv.journalfoerInngaaende.v1.to.PersistInngaaendeJournalpostTo;
import no.nav.dokarkiv.journalfoerInngaaende.v1.util.Utils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Optional;

/**
 * @author Paul Magne Lunde, Visma Consulting
 */
@Service
public class PersistInngaaendeJournalpostService {

    private JoarkRepository joarkRepository;
    private DefaultAktoerConsumerService defaultAktoerConsumerService;

    @Inject
    public PersistInngaaendeJournalpostService(JoarkRepository joarkRepository,
                                               DefaultAktoerConsumerService defaultAktoerConsumerService) {
        this.joarkRepository = joarkRepository;
        this.defaultAktoerConsumerService = defaultAktoerConsumerService;
    }

    public JournalpostResponseTo persist(PersistInngaaendeJournalpostTo persistInngaaendeJp) {

        validateRequest(persistInngaaendeJp);

        Optional<Journalpost> journalpost = joarkRepository.findById(Utils.convertStringToLong(persistInngaaendeJp.getJournalpostId(), "journalpostId"));
        if (!journalpost.isPresent()) {
            throw new DokarkivRestFunctionalException(String.format("Oppgitt journalpostId %s eksisterer ikke", persistInngaaendeJp.getJournalpostId()), HttpStatus.NOT_FOUND);
        }

        Journalpost jp = journalpost.get();
        if (!jp.isInngaende()) {
            throw new DokarkivRestFunctionalException("Journalpost er ikke av type Inngående", HttpStatus.BAD_REQUEST);
        }

        isMidlertidigJournalfoert(jp);

        HentAktoerIdForIdentResponseTo avsender = defaultAktoerConsumerService.hentAktoerIdForIdent(new HentAktoerIdForIdentRequestTo(persistInngaaendeJp.getAvsender().getIdentifikator()));
        HentAktoerIdForIdentResponseTo bruker = defaultAktoerConsumerService.hentAktoerIdForIdent(new HentAktoerIdForIdentRequestTo(persistInngaaendeJp.getBruker().getIdentifikator()));

        return JournalpostResponseTo.builder().build();
    }

    private void validateRequest(PersistInngaaendeJournalpostTo persistInngaaendeJp) {
        if (isEmpty(persistInngaaendeJp.getJournalpostId())) {
            throw new DokarkivRestFunctionalException("JournalpostId kan ikke være tom", HttpStatus.BAD_REQUEST);
        }

        try {
            FagomradeCode.valueOf(persistInngaaendeJp.getTema());
        } catch (Exception e) {
            throw new DokarkivRestFunctionalException(String.format("Tema %s eksisterer ikke", persistInngaaendeJp.getTema()), HttpStatus.BAD_REQUEST);
        }

        try {
            FagsystemCode.valueOf(persistInngaaendeJp.getArkivsak().getArkivsaksystem());
        } catch (Exception e) {
            throw new DokarkivRestFunctionalException(String.format("Fagsystem %s eksisterer ikke", persistInngaaendeJp.getArkivsak().getArkivsaksystem()), HttpStatus.BAD_REQUEST);
        }
    }

    private void isMidlertidigJournalfoert(Journalpost jp) {
        JournalStatusCode journalstatus = jp.getJournalstatus();
        if (!(journalstatus.equals(JournalStatusCode.M) || journalstatus.equals(JournalStatusCode.MO) || journalstatus.equals(JournalStatusCode.UB))) {
            throw new DokarkivRestFunctionalException("Journalpost er ikke midlertidig journalført", HttpStatus.BAD_REQUEST);
        }
    }

    private void getFoedselsnummer(AktoerTo aktoerTo) {
        HentAktoerIdForIdentResponseTo avsender = defaultAktoerConsumerService.hentAktoerIdForIdent(new HentAktoerIdForIdentRequestTo(persistInngaaendeJp.getAvsender().getIdentifikator()));
    }
}
