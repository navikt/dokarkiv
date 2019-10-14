package no.nav.dokarkiv.journalpost.v1.services;


import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.DokumentIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostDokumentInfoRelasjonIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import no.nav.dokarkiv.journalpost.v1.api.FjernVedleggTilknyttJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.validators.FjernVedleggTilknyttJournalpostValidator;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */

@Service(value = "fjernVedleggService")
@Slf4j
public class FjernVedlaggTilknyttJournalpostService {


	private final JoarkRepositorySkjermet joarkRepository;
	private final DokumentinfoRepository dokumentinfoRepository;
	private final JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;
	private final FjernVedleggTilknyttJournalpostValidator fjernVedleggTilknyttJournalpostValidator;

	@Inject
	public FjernVedlaggTilknyttJournalpostService(JoarkRepositorySkjermet joarkRepository, DokumentinfoRepository dokumentinfoRepository,
												  JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository) {
		this.joarkRepository = joarkRepository;
		this.dokumentinfoRepository = dokumentinfoRepository;
		this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
		this.fjernVedleggTilknyttJournalpostValidator = new FjernVedleggTilknyttJournalpostValidator();
	}


	public void fjernVedleggTilknyttJournalPost(String journalpostId, FjernVedleggTilknyttJournalpostRequest request) {

		fjernVedleggTilknyttJournalpostValidator.validateInput(journalpostId,request.getDokumentId());
		Long journalpostIdLong = Long.valueOf(journalpostId);
		Journalpost journalpost = joarkRepository.findById(journalpostIdLong)
				.orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Fant ikke journalpost med journalpostid=%s", journalpostId)));
		fjernVedleggTilknyttJournalpostValidator.validateJournalPostStatusOgType(journalpost);
		Long dokumentInfoId = Long.valueOf(request.getDokumentId());
		DokumentInfo dokumentInfo = hentDokumentInfo(dokumentInfoId);
		fjernVedleggTilknyttJournalpostValidator.validateDokumentInfoOriginalJpNotEqualsInputJournalPost(dokumentInfo, journalpost.getJournalpostId());
		JournalpostDokumentInfoRelasjon jpDokRelasjon = hentJournalpostDokumentRelasjon(journalpost.getJournalpostId(), dokumentInfoId);

		journalpostDokumentInfoRelasjonRepository.delete(jpDokRelasjon);

	}


	public JournalpostDokumentInfoRelasjon hentJournalpostDokumentRelasjon(Long journalpostId, Long dokumentId) {
		JournalpostDokumentInfoRelasjon jpDokRelasjon = journalpostDokumentInfoRelasjonRepository
				.findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(journalpostId, dokumentId)
				.orElseThrow(() ->
						new JournalpostDokumentInfoRelasjonIkkeFunnetException(String.
								format("Fant ikke JournalpostDokumentInfoRelasjon med journalpostId=%s dokumentId=%s", journalpostId, dokumentId)));
		fjernVedleggTilknyttJournalpostValidator.validateJournalpostDokumentInfoRelasjon(jpDokRelasjon);
		return jpDokRelasjon;
	}

	private DokumentInfo hentDokumentInfo(Long dokumentId) {

		DokumentInfo dokumentInfo = dokumentinfoRepository.findByDokumentInfoId(dokumentId)
				.orElseThrow(() -> new DokumentIkkeFunnetException(String.format("Fant ikke dokuemnt med dokumentinfoid=%s", dokumentId)));
		return dokumentInfo;
	}

}
