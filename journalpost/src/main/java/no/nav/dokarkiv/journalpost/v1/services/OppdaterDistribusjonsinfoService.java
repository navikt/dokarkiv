package no.nav.dokarkiv.journalpost.v1.services;

import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.aksjonslogg.LagreAksjonsLoggService;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterDistribusjonsinfoRequest;
import no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost.ChangeTracker;
import no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost.JournalpostUpdater;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;

import java.util.Collections;

import static no.nav.dokarkiv.journalpost.v1.validators.OppdaterDistribusjonsinfoValidator.validateJournalpost;
import static no.nav.dokarkiv.journalpost.v1.validators.OppdaterDistribusjonsinfoValidator.validateOppdaterteFelt;

@Service
@Named("oppdaterDistribusjonsinfo")
public class OppdaterDistribusjonsinfoService {

    private final JoarkRepositorySkjermet joarkRepository;
    private final JournalpostUpdater journalpostUpdater;
    private final LagreAksjonsLoggService aksjonsLoggService;

    @Inject
    public OppdaterDistribusjonsinfoService(JoarkRepositorySkjermet joarkRepository,
                                      JournalpostUpdater journalpostUpdater,
                                            LagreAksjonsLoggService aksjonsLoggService) {
        this.joarkRepository = joarkRepository;
        this.journalpostUpdater = journalpostUpdater;
        this.aksjonsLoggService = aksjonsLoggService;
    }

    public void oppdaterDistribusjonsinfo(Long journalpostId, OppdaterDistribusjonsinfoRequest request) throws UgyldigAksjonsLoggException {
        Journalpost journalpost = joarkRepository.findById(journalpostId)
                .orElseThrow(() -> new JournalpostIkkeFunnetException(
                        String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", journalpostId)));

        validateJournalpost(journalpost);
        validateOppdaterteFelt(journalpost, request);

        ChangeTracker changes = journalpostUpdater.updateFields(journalpost, request);

        joarkRepository.save(journalpost);

        if(changes.getChanges().size() > 0) {
            aksjonsLoggService.lagreAksjonsLoggForJournalpost(
                    AksjonsTypeCode.EKSPEDER, journalpostId, null, "Journalposten fikk status 'ekspedert'",
                    null, changes.getChanges());
        }
    }

}
