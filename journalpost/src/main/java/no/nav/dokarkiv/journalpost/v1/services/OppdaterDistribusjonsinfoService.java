package no.nav.dokarkiv.journalpost.v1.services;

import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterDistribusjonsinfoRequest;
import no.nav.dokarkiv.journalpost.v1.util.AksjonsLoggHelper;
import no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost.JournalpostUpdater;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;

import static no.nav.dokarkiv.journalpost.v1.validators.OppdaterDistribusjonsinfoValidator.validateJournalpost;
import static no.nav.dokarkiv.journalpost.v1.validators.OppdaterDistribusjonsinfoValidator.validateOppdaterteFelt;

@Service
@Named("oppdaterDistribusjonsinfo")
public class OppdaterDistribusjonsinfoService {

    private final JoarkRepositorySkjermet joarkRepository;
    private final JournalpostUpdater journalpostUpdater;
    private final AksjonsLoggService aksjonsLoggService;

    @Inject
    public OppdaterDistribusjonsinfoService(JoarkRepositorySkjermet joarkRepository,
                                      JournalpostUpdater journalpostUpdater,
                                      AksjonsLoggService aksjonsLoggService) {
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
        AksjonsLoggHelper aksjonsLoggHelperJournalpost = new AksjonsLoggHelper();
        journalpostUpdater.updateFields(journalpost, request, aksjonsLoggHelperJournalpost);
        joarkRepository.save(journalpost);
        saveAksjonslogg(aksjonsLoggHelperJournalpost);
    }

    private void saveAksjonslogg(AksjonsLoggHelper aksjonsLoggHelper) throws UgyldigAksjonsLoggException {
        if (!aksjonsLoggHelper.getArkivElementEndringTOs().isEmpty()) {
            aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggHelper.getAksjonsLoggTO(), aksjonsLoggHelper
                    .getArkivElementEndringTOs());
        }
    }


}
