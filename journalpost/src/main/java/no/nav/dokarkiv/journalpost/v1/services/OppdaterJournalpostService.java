package no.nav.dokarkiv.journalpost.v1.services;

import static no.nav.dokarkiv.journalpost.v1.validators.OppdaterJournalpostValidator.validateOppdaterteFelt;

import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.DokumentIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import no.nav.dokarkiv.journalpost.v1.AksjonsLoggHelper;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.oppdaterjournalpost.DokumentInfoUpdater;
import no.nav.dokarkiv.journalpost.v1.oppdaterjournalpost.JournalpostUpdater;
import no.nav.dokarkiv.journalpost.v1.oppdaterjournalpost.SaksrelasjonUpdater;
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
	private final AksjonsLoggService aksjonsLoggService;

	@Inject
    public OppdaterJournalpostService(JoarkRepositorySkjermet joarkRepository,
									  JournalpostUpdater journalpostUpdater,
									  SaksrelasjonUpdater saksrelasjonUpdater,
									  DokumentinfoRepository dokumentinfoRepository,
									  DokumentInfoUpdater dokumentInfoUpdater,
									  AksjonsLoggService aksjonsLoggService) {
		this.joarkRepository = joarkRepository;
		this.dokumentinfoRepository = dokumentinfoRepository;
		this.journalpostUpdater = journalpostUpdater;
		this.saksrelasjonUpdater = saksrelasjonUpdater;
		this.dokumentInfoUpdater = dokumentInfoUpdater;
		this.aksjonsLoggService = aksjonsLoggService;
	}

	public void oppdaterJournalpost(Long journalpostId, OppdaterJournalpostRequest oppdaterJournalpostRequest) throws UgyldigAksjonsLoggException {
		Journalpost journalpost = joarkRepository.findById(journalpostId)
				.orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", journalpostId)));

		AksjonsLoggHelper.setJournalpostId(journalpostId);
		AksjonsLoggHelper.setBrukerId(oppdaterJournalpostRequest.getBruker() != null ?
				oppdaterJournalpostRequest.getBruker().getId() :
				(journalpost.getBrukere().isEmpty() ? null : journalpost.getBrukere().iterator().next().getBrukerId())
		);

		validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost.getJournalstatus());

		AksjonsLoggHelper aksjonsLoggHelperJournalpost = new AksjonsLoggHelper();
		journalpostUpdater.updateFields(journalpost, oppdaterJournalpostRequest, aksjonsLoggHelperJournalpost);

		AksjonsLoggHelper aksjonsLoggHelperSaksrelasjon = new AksjonsLoggHelper();
		saksrelasjonUpdater.updateFields(journalpost, oppdaterJournalpostRequest, aksjonsLoggHelperSaksrelasjon);

		joarkRepository.save(journalpost);
		saveAksjonslogg(aksjonsLoggHelperJournalpost);
		saveAksjonslogg(aksjonsLoggHelperSaksrelasjon);

		if (oppdaterJournalpostRequest.getDokumenter() != null) {
			for (no.nav.dokarkiv.journalpost.v1.api.DokumentInfo dokument : oppdaterJournalpostRequest.getDokumenter()) {
				DokumentInfo dokumentInfo = journalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(Long.parseLong(dokument.getDokumentInfoId()));
				assertDokumentInfoNotNull(dokumentInfo, String.valueOf(journalpost.getJournalpostId()), dokument.getDokumentInfoId());

				AksjonsLoggHelper aksjonsLoggHelperDokument = new AksjonsLoggHelper();
				dokumentInfoUpdater.updateFields(dokumentInfo, dokument, aksjonsLoggHelperDokument);

				dokumentinfoRepository.save(dokumentInfo);
				saveAksjonslogg(aksjonsLoggHelperDokument);
			}
		}
	}

	private void assertDokumentInfoNotNull(DokumentInfo dokumentInfo, String journalpostId, String dokumentId) {
		if (dokumentInfo == null) {
			throw new DokumentIkkeFunnetException(String.format("Fant ingen dokument med dokumentId=%s paa journalpost med journalpostId=%s", dokumentId, journalpostId));
		}
	}

	private void saveAksjonslogg(AksjonsLoggHelper aksjonsLoggHelper) throws UgyldigAksjonsLoggException {
		if (!aksjonsLoggHelper.getArkivElementEndringTOs().isEmpty()) {
			aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggHelper.getAksjonsLoggTO(), aksjonsLoggHelper
					.getArkivElementEndringTOs());
		}
	}
}
