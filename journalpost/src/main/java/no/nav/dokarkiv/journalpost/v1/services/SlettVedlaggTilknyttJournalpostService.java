package no.nav.dokarkiv.journalpost.v1.services;


import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.DokumentIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostDokumentInfoRelasjonIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import no.nav.dokarkiv.journalpost.v1.api.FjernVedleggTilknyttJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.validators.SlettVedleggTilknyttJournalpostValidator;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */

@Service(value = "fjernVedleggService")
@Slf4j
public class SlettVedlaggTilknyttJournalpostService {


	private final JoarkRepositorySkjermet joarkRepository;
	private final DokumentinfoRepository dokumentinfoRepository;
	private final DokumentFilRepository dokumentFilRepository;
	private final JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;
	private final SlettVedleggTilknyttJournalpostValidator slettVedleggTilknyttJournalpostValidator;

	@Inject
	public SlettVedlaggTilknyttJournalpostService(JoarkRepositorySkjermet joarkRepository, DokumentinfoRepository dokumentinfoRepository,
												  DokumentFilRepository dokumentFilRepository, JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository,
												  SlettVedleggTilknyttJournalpostValidator slettVedleggTilknyttJournalpostValidator) {
		this.joarkRepository = joarkRepository;
		this.dokumentinfoRepository = dokumentinfoRepository;
		this.dokumentFilRepository = dokumentFilRepository;
		this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
		this.slettVedleggTilknyttJournalpostValidator = slettVedleggTilknyttJournalpostValidator;
	}


	public void slettVedleggTilknyttJournalPost(String journalpostId, FjernVedleggTilknyttJournalpostRequest request) {

		Long journalpostIdLong = Long.valueOf(journalpostId);
		Journalpost journalpost = joarkRepository.findById(journalpostIdLong)
				.orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Fant ikke journalpost med journalpostid=%s", journalpostId)));
		slettVedleggTilknyttJournalpostValidator.validateJournalPostStatusOgType(journalpost);
		Long dokumentInfoId = Long.valueOf(request.getDokumentId());
		DokumentInfo dokumentInfo = hentDokumentInfo(dokumentInfoId);
		slettVedleggTilknyttJournalpostValidator.validateDokumentInfoOriginalJpNotEqualsInputJournalPost(dokumentInfo, journalpost.getJournalpostId());
		JournalpostDokumentInfoRelasjon jpDokRelasjon = hentJournalpostDokumentRelasjon(journalpost.getJournalpostId(), dokumentInfoId);

		journalpostDokumentInfoRelasjonRepository.delete(jpDokRelasjon);

	}


	public JournalpostDokumentInfoRelasjon hentJournalpostDokumentRelasjon(Long journalpostId, Long dokumentId) {
		JournalpostDokumentInfoRelasjon jpDokRelasjon = journalpostDokumentInfoRelasjonRepository
				.findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(journalpostId, dokumentId)
				.orElseThrow(() ->
						new JournalpostDokumentInfoRelasjonIkkeFunnetException(String.
								format("Fant ikke JournalpostDokumentInfoRelasjon med journalpostId=%s dokumentId=%s", journalpostId, dokumentId)));
		slettVedleggTilknyttJournalpostValidator.validateJournalpostDokumentInfoRelasjon(jpDokRelasjon);
		return jpDokRelasjon;
	}

	private DokumentInfo hentDokumentInfo(Long dokumentId) {

		DokumentInfo dokumentInfo = dokumentinfoRepository.findByDokumentInfoId(dokumentId)
				.orElseThrow(() -> new DokumentIkkeFunnetException(String.format("Fant ikke dokuemnt med dokumentinfoid=%s", dokumentId)));
		return dokumentInfo;
	}

}
