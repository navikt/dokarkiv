package no.nav.dokarkiv.journalpost.v1.rjoark200;

import static no.nav.dokarkiv.journalpost.v1.rjoark200.OppdaterJournalpostValidator.validateOppdaterteFelt;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.DokumentIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostRequest;
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

	@Inject
    public OppdaterJournalpostService(JoarkRepositorySkjermet joarkRepository,
									  JournalpostUpdater journalpostUpdater,
									  SaksrelasjonUpdater saksrelasjonUpdater,
									  DokumentinfoRepository dokumentinfoRepository,
									  DokumentInfoUpdater dokumentInfoUpdater) {
		this.joarkRepository = joarkRepository;
		this.dokumentinfoRepository = dokumentinfoRepository;
		this.journalpostUpdater = journalpostUpdater;
		this.saksrelasjonUpdater = saksrelasjonUpdater;
		this.dokumentInfoUpdater = dokumentInfoUpdater;
	}

	public void oppdaterJournalpost(Long journalpostId, OppdaterJournalpostRequest oppdaterJournalpostRequest, String aksjonsLoggHeaderString) throws UgyldigAksjonsLoggException {
		Journalpost journalpost = joarkRepository.findById(journalpostId)
				.orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", journalpostId)));

		AksjonsLoggHelper.setAksjonsLoggHeaderString(aksjonsLoggHeaderString);
		AksjonsLoggHelper.setJournalpostId(journalpostId);
		AksjonsLoggHelper.setBrukerId(oppdaterJournalpostRequest.getBruker() != null ?
				oppdaterJournalpostRequest.getBruker().getId() :
				(journalpost.getBrukere().isEmpty() ? null : journalpost.getBrukere().iterator().next().getBrukerId())
		);

		validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost.getJournalstatus());
		journalpostUpdater.updateFields(journalpost, oppdaterJournalpostRequest);
		saksrelasjonUpdater.updateFields(journalpost, oppdaterJournalpostRequest);
		joarkRepository.save(journalpost);

		if (oppdaterJournalpostRequest.getDokumenter() != null) {
			for (no.nav.dokarkiv.journalpost.v1.api.DokumentInfo dokument : oppdaterJournalpostRequest.getDokumenter()) {
				DokumentInfo dokumentInfo = journalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(Long.parseLong(dokument.getDokumentInfoId()));

				assertDokumentInfoNotNull(dokumentInfo, String.valueOf(journalpost.getJournalpostId()), dokument.getDokumentInfoId());
				dokumentInfoUpdater.updateFields(dokumentInfo, dokument);
				dokumentinfoRepository.save(dokumentInfo);
			}
		}
	}

	private void assertDokumentInfoNotNull(DokumentInfo dokumentInfo, String journalpostId, String dokumentId) {
		if (dokumentInfo == null) {
			throw new DokumentIkkeFunnetException(String.format("Fant ingen dokument med dokumentId=%s paa journalpost med journalpostId=%s", dokumentId, journalpostId));
		}
	}
}
