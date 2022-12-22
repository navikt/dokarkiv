package no.nav.dokarkiv.journalpost.v1.services;


import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.DokumentIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostDokumentInfoRelasjonIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.repository.DokumentInfoRepository;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import no.nav.dokarkiv.journalpost.v1.api.FjernVedleggTilknyttetJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.validators.FjernVedleggTilknyttetJournalpostValidator;
import org.springframework.stereotype.Service;

@Service(value = "fjernVedleggService")
@Slf4j
public class FjernVedleggTilknyttetJournalpost {

	private final JoarkRepositorySkjermet joarkRepository;
	private final DokumentInfoRepository dokumentInfoRepository;
	private final JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;
	private final FjernVedleggTilknyttetJournalpostValidator fjernVedleggTilknyttetJournalpostValidator;

	public FjernVedleggTilknyttetJournalpost(JoarkRepositorySkjermet joarkRepository, DokumentInfoRepository dokumentInfoRepository,
											 JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository) {
		this.joarkRepository = joarkRepository;
		this.dokumentInfoRepository = dokumentInfoRepository;
		this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
		this.fjernVedleggTilknyttetJournalpostValidator = new FjernVedleggTilknyttetJournalpostValidator();
	}

	public void fjernVedleggTilknyttetJournalpost(String journalpostId, FjernVedleggTilknyttetJournalpostRequest request) {
		fjernVedleggTilknyttetJournalpostValidator.validateInput(journalpostId,request.getDokumentId());
		Long dokumentInfoId = Long.valueOf(request.getDokumentId());
		Journalpost journalpost = joarkRepository.findById(Long.valueOf(journalpostId))
				.orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Fant ikke journalpost med journalpostId=%s", journalpostId)));
		fjernVedleggTilknyttetJournalpostValidator.validateJournalPostStatusOgType(journalpost);
		DokumentInfo dokumentInfo = hentDokumentInfo(dokumentInfoId);
		fjernVedleggTilknyttetJournalpostValidator.validateDokumentInfoOriginalJpNotEqualsInputJournalpost(dokumentInfo, journalpost.getJournalpostId());
		JournalpostDokumentInfoRelasjon jpDokRelasjon = hentJournalpostDokumentRelasjon(journalpost.getJournalpostId(), dokumentInfoId);

		journalpostDokumentInfoRelasjonRepository.delete(jpDokRelasjon);
	}

	public JournalpostDokumentInfoRelasjon hentJournalpostDokumentRelasjon(Long journalpostId, Long dokumentId) {
		JournalpostDokumentInfoRelasjon jpDokRelasjon = journalpostDokumentInfoRelasjonRepository
				.findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(journalpostId, dokumentId)
				.orElseThrow(() ->
						new JournalpostDokumentInfoRelasjonIkkeFunnetException(String.
								format("Fant ikke JournalpostDokumentInfoRelasjon med journalpostId=%s dokumentInfoId=%s", journalpostId, dokumentId)));
		fjernVedleggTilknyttetJournalpostValidator.validateJournalpostDokumentInfoRelasjon(jpDokRelasjon);
		return jpDokRelasjon;
	}

	private DokumentInfo hentDokumentInfo(Long dokumentId) {
		return dokumentInfoRepository.findById(dokumentId)
				.orElseThrow(() -> new DokumentIkkeFunnetException(String.format("Fant ikke dokument med dokumentInfoId=%s", dokumentId)));
	}
}
