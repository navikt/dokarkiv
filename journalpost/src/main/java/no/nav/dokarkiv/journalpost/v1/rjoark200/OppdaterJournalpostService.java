package no.nav.dokarkiv.journalpost.v1.rjoark200;

import static no.nav.dokarkiv.journalpost.v1.rjoark200.OppdaterJournalpostValidator.validateOppdaterteFelt;
import static no.nav.dokarkiv.journalpost.v1.rjoark200.util.Utils.convertStringToLong;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.rjoark200.util.Utils;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;


@Service
@Named("oppdaterMetadataJournalpost")
public class OppdaterJournalpostService {

    private final JoarkRepositorySkjermet joarkRepository;
    private final DokumentinfoRepository dokumentinfoRepository;
	private final JournalpostMapper journalpostMapper;
	private final DokumentInfoMapper dokumentInfoMapper;

	@Inject
    public OppdaterJournalpostService(JoarkRepositorySkjermet joarkRepository,
									  JournalpostMapper journalpostMapper,
									  DokumentinfoRepository dokumentinfoRepository,
									  DokumentInfoMapper dokumentInfoMapper) {
		this.joarkRepository = joarkRepository;
		this.dokumentinfoRepository = dokumentinfoRepository;
		this.journalpostMapper = journalpostMapper;
		this.dokumentInfoMapper = dokumentInfoMapper;
	}

	public void oppdaterJournalpost(String journalpostId, OppdaterJournalpostRequest oppdaterJournalpostRequest, String aksjonsLoggHeaderString) throws UgyldigAksjonsLoggException {
		Journalpost journalpost = joarkRepository.findById(convertStringToLong(journalpostId, "journalpostId"))
				.orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", journalpostId)));

		AksjonsLoggHelper.setAksjonsLoggHeaderString(aksjonsLoggHeaderString);
		AksjonsLoggHelper.setJournalpostId(Long.parseLong(journalpostId));
		AksjonsLoggHelper.setBrukerId(oppdaterJournalpostRequest.getBruker() != null ?
				oppdaterJournalpostRequest.getBruker().getId() :
				(journalpost.getBrukere().isEmpty() ? null : journalpost.getBrukere().iterator().next().getBrukerId())
		);

		validateOppdaterteFelt(oppdaterJournalpostRequest, journalpost.getJournalstatus());
		journalpostMapper.oppdaterJournalpost(journalpost, oppdaterJournalpostRequest);
		joarkRepository.save(journalpost);

		if (oppdaterJournalpostRequest.getDokumenter() != null) {
			for (no.nav.dokarkiv.journalpost.v1.api.DokumentInfo dokument : oppdaterJournalpostRequest.getDokumenter()) {
				DokumentInfo dokumentInfo = journalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(Long.parseLong(dokument.getDokumentInfoId()));

				Utils.assertDokumentInfoNotNull(dokumentInfo, String.valueOf(journalpost.getJournalpostId()), dokument.getDokumentInfoId());
				dokumentInfoMapper.oppdaterDokumentInfo(dokumentInfo, dokument.getBrevkode(), dokument.getTittel());
				dokumentinfoRepository.save(dokumentInfo);
			}
		}
	}
}
