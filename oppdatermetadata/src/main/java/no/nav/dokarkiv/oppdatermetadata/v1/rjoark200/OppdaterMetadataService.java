package no.nav.dokarkiv.oppdatermetadata.v1.rjoark200;

import static no.nav.dokarkiv.oppdatermetadata.v1.support.OppdaterMetadataValidator.validateOppdaterteFelt;
import static no.nav.dokarkiv.oppdatermetadata.v1.util.Utils.convertStringToLong;

import no.nav.dok.oppdatermetadata.api.v1.PutOppdatermetadataRequest;
import no.nav.dok.oppdatermetadata.api.v1.PutOppdatermetadataResponse;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import no.nav.dokarkiv.oppdatermetadata.v1.util.Utils;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Paul Magne Lunde, Visma Consulting
 */
@Service
public class OppdaterMetadataService {

    private final JoarkRepositorySkjermet joarkRepository;
    private final DokumentinfoRepository dokumentinfoRepository;
	private final JournalpostMapper journalpostMapper;
	private final DokumentInfoMapper dokumentInfoMapper;

	@Inject
    public OppdaterMetadataService(JoarkRepositorySkjermet joarkRepository,
                                   JournalpostMapper journalpostMapper,
                                   DokumentinfoRepository dokumentinfoRepository,
                                   DokumentInfoMapper dokumentInfoMapper) {
		this.joarkRepository = joarkRepository;
		this.dokumentinfoRepository = dokumentinfoRepository;
		this.journalpostMapper = journalpostMapper;
		this.dokumentInfoMapper = dokumentInfoMapper;
	}

	public PutOppdatermetadataResponse updateInngaaendeJournalpost(String journalpostId, PutOppdatermetadataRequest putOppdatermetadataRequest) throws InputValideringFeiletException {
		Journalpost journalpost = joarkRepository.findById(convertStringToLong(journalpostId, "journalpostId"))
				.orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", journalpostId)));

		validateOppdaterteFelt(putOppdatermetadataRequest, journalpost.getJournalstatus());
		journalpostMapper.oppdaterJournalpost(journalpost, putOppdatermetadataRequest);
		joarkRepository.save(journalpost);

		if (putOppdatermetadataRequest.getDokumentInfoList() != null) {
			for (no.nav.dok.oppdatermetadata.api.v1.DokumentInfo dokument : putOppdatermetadataRequest.getDokumentInfoList()) {
				DokumentInfo dokumentInfo = journalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(Long.parseLong(dokument.getDokumentInfoId()));

				Utils.assertDokumentInfoNotNull(dokumentInfo, String.valueOf(journalpost.getJournalpostId()), dokument.getDokumentInfoId());
				dokumentInfoMapper.oppdaterDokumentInfo(dokumentInfo, dokument.getBrevkode(), dokument.getTittel());
				dokumentinfoRepository.save(dokumentInfo);
			}
		}

        return PutOppdatermetadataResponse.builder()
				.journalpostId(journalpostId)
				.build();
	}
}
