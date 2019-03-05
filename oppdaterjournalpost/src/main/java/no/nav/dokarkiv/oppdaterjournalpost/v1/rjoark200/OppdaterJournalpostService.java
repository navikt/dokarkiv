package no.nav.dokarkiv.oppdaterjournalpost.v1.rjoark200;

import static no.nav.dokarkiv.oppdaterjournalpost.v1.support.OppdaterJournalpostValidator.validateOppdaterteFelt;
import static no.nav.dokarkiv.oppdaterjournalpost.v1.util.Utils.convertStringToLong;

import no.nav.dok.oppdaterjournalpost.api.v1.PutOppdaterJournalpostRequest;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import no.nav.dokarkiv.oppdaterjournalpost.v1.support.AksjonsloggHelper;
import no.nav.dokarkiv.oppdaterjournalpost.v1.util.Utils;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Paul Magne Lunde, Visma Consulting
 */
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

	public void oppdaterJournalpost(String journalpostId, PutOppdaterJournalpostRequest putOppdaterJournalpostRequest, String aksjonsLoggHeaderString) throws InputValideringFeiletException, UgyldigAksjonsLoggException {
		Journalpost journalpost = joarkRepository.findById(convertStringToLong(journalpostId, "journalpostId"))
				.orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", journalpostId)));

		AksjonsloggHelper.setAksjonsLoggHeaderString(aksjonsLoggHeaderString);
		AksjonsloggHelper.setJournalpostId(Long.parseLong(journalpostId));
		AksjonsloggHelper.setBrukerId(putOppdaterJournalpostRequest.getBruker() != null ?
				putOppdaterJournalpostRequest.getBruker().getIdentifikator() :
				(journalpost.getBrukere().isEmpty() ? null : journalpost.getBrukere().iterator().next().getBrukerId())
		);

		validateOppdaterteFelt(putOppdaterJournalpostRequest, journalpost.getJournalstatus());
		journalpostMapper.oppdaterJournalpost(journalpost, putOppdaterJournalpostRequest);
		joarkRepository.save(journalpost);

		if (putOppdaterJournalpostRequest.getDokumentInfoList() != null) {
			for (no.nav.dok.oppdaterjournalpost.api.v1.DokumentInfo dokument : putOppdaterJournalpostRequest.getDokumentInfoList()) {
				DokumentInfo dokumentInfo = journalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(Long.parseLong(dokument.getDokumentInfoId()));

				Utils.assertDokumentInfoNotNull(dokumentInfo, String.valueOf(journalpost.getJournalpostId()), dokument.getDokumentInfoId());
				dokumentInfoMapper.oppdaterDokumentInfo(dokumentInfo, dokument.getBrevkode(), dokument.getTittel());
				dokumentinfoRepository.save(dokumentInfo);
			}
		}
	}
}
