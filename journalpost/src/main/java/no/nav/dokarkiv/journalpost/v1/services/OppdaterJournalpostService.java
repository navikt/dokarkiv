package no.nav.dokarkiv.journalpost.v1.services;

import static no.nav.dokarkiv.journalpost.v1.JournalpostApiConfig.RETRY_DELAY;
import static no.nav.dokarkiv.journalpost.v1.JournalpostApiConfig.RETRY_MULTIPLIER;
import static no.nav.dokarkiv.journalpost.v1.validators.OppdaterJournalpostValidator.validateOppdaterteFelt;

import no.nav.dokarkiv.core.aksjonslogg.LagreAksjonsLoggService;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.DokumentIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost.ChangeTracker;
import no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost.DokumentInfoUpdater;
import no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost.JournalpostUpdater;
import no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost.SaksrelasjonUpdater;
import org.hibernate.StaleObjectStateException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;

@Service
@Named("oppdaterMetadataJournalpost")
public class OppdaterJournalpostService {

    private final JoarkRepositorySkjermet joarkRepository;
    private final DokumentinfoRepository dokumentinfoRepository;
	private final JournalpostUpdater journalpostUpdater;
	private final SaksrelasjonUpdater saksrelasjonUpdater;
	private final DokumentInfoUpdater dokumentInfoUpdater;
	private final LagreAksjonsLoggService lagreAksjonsLoggService;

	@Inject
    public OppdaterJournalpostService(JoarkRepositorySkjermet joarkRepository,
									  JournalpostUpdater journalpostUpdater,
									  SaksrelasjonUpdater saksrelasjonUpdater,
									  DokumentinfoRepository dokumentinfoRepository,
									  DokumentInfoUpdater dokumentInfoUpdater,
									  LagreAksjonsLoggService lagreAksjonsLoggService) {
		this.joarkRepository = joarkRepository;
		this.dokumentinfoRepository = dokumentinfoRepository;
		this.journalpostUpdater = journalpostUpdater;
		this.saksrelasjonUpdater = saksrelasjonUpdater;
		this.dokumentInfoUpdater = dokumentInfoUpdater;
		this.lagreAksjonsLoggService = lagreAksjonsLoggService;
	}

	@Retryable(
			include = {ObjectOptimisticLockingFailureException.class, StaleObjectStateException.class},
			backoff = @Backoff(delay = RETRY_DELAY, multiplier = RETRY_MULTIPLIER)
	)
	public void oppdaterJournalpost(Long journalpostId, OppdaterJournalpostRequest oppdaterJournalpostRequest) {
		Journalpost journalpost = joarkRepository.findById(journalpostId)
				.orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", journalpostId)));

		validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost.getJournalstatus(), journalpost.getJournalposttype());

		ChangeTracker changeTracker = journalpostUpdater.updateFields(journalpost, oppdaterJournalpostRequest);

		if(!changeTracker.getChanges().isEmpty()) {
			lagreAksjonsLoggService.lagreAksjonsLoggForJournalpost(
					AksjonsTypeCode.ENDRE_METADATA, journalpostId, null,
					hentMeldingFraAksjonsType(AksjonsTypeCode.ENDRE_METADATA), null, changeTracker.getChanges());
		}

		changeTracker = saksrelasjonUpdater.updateFields(journalpost, oppdaterJournalpostRequest);
		joarkRepository.save(journalpost);
		if(!changeTracker.getChanges().isEmpty()) {
			lagreAksjonsLoggService.lagreAksjonsLoggForJournalpost(
					AksjonsTypeCode.SAKSTILKNYTNING, journalpostId, null,
					hentMeldingFraAksjonsType(AksjonsTypeCode.SAKSTILKNYTNING), null, changeTracker.getChanges());
		}

		if (oppdaterJournalpostRequest.getDokumenter() != null) {
			for (no.nav.dokarkiv.journalpost.v1.api.DokumentInfo dokument : oppdaterJournalpostRequest.getDokumenter()) {
				DokumentInfo dokumentInfo = journalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(Long.parseLong(dokument.getDokumentInfoId()));
				assertDokumentInfoNotNull(dokumentInfo, String.valueOf(journalpost.getJournalpostId()), dokument.getDokumentInfoId());

				changeTracker = dokumentInfoUpdater.updateFields(dokumentInfo, dokument);
				dokumentinfoRepository.save(dokumentInfo);
				if(!changeTracker.getChanges().isEmpty()) {
					lagreAksjonsLoggService.lagreAksjonsLogg(
							AksjonsTypeCode.ENDRE_METADATA, dokumentInfo.getDokumentInfoId(), null,
							hentMeldingFraAksjonsType(AksjonsTypeCode.ENDRE_METADATA), null, changeTracker.getChanges());
				}
			}
		}
	}

	private String hentMeldingFraAksjonsType(AksjonsTypeCode kode) {
		return kode.equals(AksjonsTypeCode.SAKSTILKNYTNING) ?
				"Journalposten ble knyttet til en sak." :
				"Metadata på journalposten ble endretFlagg.";
	}

	private void assertDokumentInfoNotNull(DokumentInfo dokumentInfo, String journalpostId, String dokumentId) {
		if (dokumentInfo == null) {
			throw new DokumentIkkeFunnetException(String.format("Fant ingen dokument med dokumentId=%s paa journalpost med journalpostId=%s", dokumentId, journalpostId));
		}
	}

}
