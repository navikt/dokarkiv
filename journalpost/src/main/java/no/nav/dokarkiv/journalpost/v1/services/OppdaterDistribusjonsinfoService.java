package no.nav.dokarkiv.journalpost.v1.services;

import no.nav.dokarkiv.core.aksjonslogg.LagreAksjonsLoggService;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterDistribusjonsinfoRequest;
import no.nav.dokarkiv.journalpost.v1.api.bulkOppdaterDistribusjonsinfo.JournalpostResponse;
import no.nav.dokarkiv.journalpost.v1.api.bulkOppdaterDistribusjonsinfo.JournalpostWithDistribusjonsinfo;
import no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost.ChangeTracker;
import no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost.JournalpostUpdater;
import no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost.JournalpostUpdaterFromBulk;
import org.springframework.stereotype.Service;

import static no.nav.dokarkiv.journalpost.v1.validators.OppdaterDistribusjonsinfoValidator.validateJournalpostKanSetteStatusEkspedert;

@Service("oppdaterDistribusjonsinfo")
public class OppdaterDistribusjonsinfoService {

    private final JoarkRepositorySkjermet joarkRepository;
    private final JournalpostUpdater journalpostUpdater;
    private final LagreAksjonsLoggService aksjonsLoggService;

    public OppdaterDistribusjonsinfoService(JoarkRepositorySkjermet joarkRepository,
                                      JournalpostUpdater journalpostUpdater,
                                            LagreAksjonsLoggService aksjonsLoggService) {
        this.joarkRepository = joarkRepository;
        this.journalpostUpdater = journalpostUpdater;
        this.aksjonsLoggService = aksjonsLoggService;
    }

    public void oppdaterDistribusjonsinfo(Long journalpostId, OppdaterDistribusjonsinfoRequest request) {
        Journalpost journalpost = joarkRepository.findById(journalpostId)
                .orElseThrow(() -> new JournalpostIkkeFunnetException(
                        String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", journalpostId)));

        if (request.getSettStatusEkspedert()) {
            validateJournalpostKanSetteStatusEkspedert(journalpost, request);
        }

        ChangeTracker trackStatusSattTilEkspedert = journalpostUpdater.updateFields(journalpost, request);

        joarkRepository.save(journalpost);

        if(!trackStatusSattTilEkspedert.getChanges().isEmpty()) {
            aksjonsLoggService.lagreAksjonsLoggForJournalpost(
                    AksjonsTypeCode.EKSPEDER, journalpostId, null, "Journalposten fikk status 'ekspedert'",
                    null, trackStatusSattTilEkspedert.getChanges());
        }
    }

	public JournalpostResponse oppdaterDistribusjonsinfoFromBulk(JournalpostWithDistribusjonsinfo journalpostWithDistribusjonsinfo) {
		return joarkRepository.findById(journalpostWithDistribusjonsinfo.getJournalpostId()).map(journalpost -> {
			try {
				if (journalpostWithDistribusjonsinfo.getSettStatusEkspedert()) {
					validateJournalpostKanSetteStatusEkspedert(journalpost, journalpostWithDistribusjonsinfo);
				}

				ChangeTracker trackStatusSattTilEkspedert = JournalpostUpdaterFromBulk.updateFields(journalpost, journalpostWithDistribusjonsinfo);

				joarkRepository.save(journalpost);

				if (!trackStatusSattTilEkspedert.getChanges().isEmpty()) {
					aksjonsLoggService.lagreAksjonsLoggForJournalpost(
							AksjonsTypeCode.EKSPEDER, journalpost.getJournalpostId(), null, "Journalposten fikk status 'ekspedert'",
							null, trackStatusSattTilEkspedert.getChanges());
				}
				return new JournalpostResponse(journalpostWithDistribusjonsinfo.getJournalpostId(), null);
			} catch (DokarkivFunctionalException e) {
				return new JournalpostResponse(journalpostWithDistribusjonsinfo.getJournalpostId(), e.getMessage());
			}
		}).orElseGet(() -> new JournalpostResponse(journalpostWithDistribusjonsinfo.getJournalpostId(),
				String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", journalpostWithDistribusjonsinfo.getJournalpostId())));
	}

}
