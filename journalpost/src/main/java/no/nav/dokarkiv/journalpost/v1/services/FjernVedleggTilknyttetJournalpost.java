package no.nav.dokarkiv.journalpost.v1.services;


import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.DokumentIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostDokumentInfoRelasjonIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.repository.DokumentInfoRepository;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import no.nav.dokarkiv.core.repository.JournalpostRepositorySkjermet;
import no.nav.dokarkiv.journalpost.v1.api.FjernVedleggTilknyttetJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.validators.FjernVedleggTilknyttetJournalpostValidator;
import org.springframework.stereotype.Service;

import static java.lang.String.format;
import static no.nav.dokarkiv.journalpost.v1.validators.CommonValidator.validateIdAndParse;

@Service(value = "fjernVedleggService")
@Slf4j
public class FjernVedleggTilknyttetJournalpost {

	private final JournalpostRepositorySkjermet journalpostRepositorySkjermet;
	private final DokumentInfoRepository dokumentInfoRepository;
	private final JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;
	private final FjernVedleggTilknyttetJournalpostValidator fjernVedleggTilknyttetJournalpostValidator;

	public FjernVedleggTilknyttetJournalpost(JournalpostRepositorySkjermet journalpostRepositorySkjermet, DokumentInfoRepository dokumentInfoRepository,
											 JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository) {
		this.journalpostRepositorySkjermet = journalpostRepositorySkjermet;
		this.dokumentInfoRepository = dokumentInfoRepository;
		this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
		this.fjernVedleggTilknyttetJournalpostValidator = new FjernVedleggTilknyttetJournalpostValidator();
	}

	public void fjernVedleggTilknyttetJournalpost(long journalpostId, FjernVedleggTilknyttetJournalpostRequest request) {
		long dokumentInfoId = validateIdAndParse(request.getDokumentId(),"dokumentId");
		Journalpost journalpost = journalpostRepositorySkjermet.findById(journalpostId)
				.orElseThrow(() -> new JournalpostIkkeFunnetException("Fant ikke journalpost"));
		fjernVedleggTilknyttetJournalpostValidator.validateJournalPostStatusOgType(journalpost);
		DokumentInfo dokumentInfo = hentDokumentInfo(dokumentInfoId, journalpostId);
		fjernVedleggTilknyttetJournalpostValidator.validateDokumentInfoOriginalJpNotEqualsInputJournalpost(dokumentInfo, journalpost.getJournalpostId());
		JournalpostDokumentInfoRelasjon jpDokRelasjon = hentJournalpostDokumentRelasjon(journalpost.getJournalpostId(), dokumentInfoId);

		journalpostDokumentInfoRelasjonRepository.delete(jpDokRelasjon);
	}

	public JournalpostDokumentInfoRelasjon hentJournalpostDokumentRelasjon(long journalpostId, long dokumentId) {
		JournalpostDokumentInfoRelasjon jpDokRelasjon = journalpostDokumentInfoRelasjonRepository
				.findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(journalpostId, dokumentId)
				.orElseThrow(() ->
						new JournalpostDokumentInfoRelasjonIkkeFunnetException(format("Fant ikke JournalpostDokumentInfoRelasjon med journalpostId=%s og dokumentId=%s",
								journalpostId,
								dokumentId)));

		fjernVedleggTilknyttetJournalpostValidator.validateJournalpostDokumentInfoRelasjon(jpDokRelasjon);
		return jpDokRelasjon;
	}

	private DokumentInfo hentDokumentInfo(long dokumentId, long journalpostId) {
		return dokumentInfoRepository.findById(dokumentId)
				.orElseThrow(() -> new DokumentIkkeFunnetException(format(
						"Fant ikke dokument med dokumentId=%s, og kan ikke fjerne dette som vedlegg fra journalpost med journalpostId=%s", dokumentId, journalpostId)));
	}
}
