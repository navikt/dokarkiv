package no.nav.dokarkiv.slettdokument.service;

import static no.nav.dokarkiv.slettdokument.SlettDokumentRestController.REQUEST_ID;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import no.nav.dokarkiv.slettdokument.SlettDokumentResponse;
import no.nav.dokarkiv.slettdokument.SlettDokumentResponseMapper;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

/**
 * Implementation of SlettDokument
 */
@Service
@Slf4j
public class DefaultSlettDokumentService implements SlettDokumentService {

	@Inject
	private DefaultSlettDokumentValidator validator;

	@Inject
	private DokumentinfoRepository dokumentinfoRepository;

	@Inject
	private JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;

	@Override
	public SlettDokumentResponse slettDokument(SlettDokumentRequestTo requestTo) {
		validator.validateInputRequest(requestTo);

		List<JournalpostDokumentInfoRelasjon> jpDokInfoRelasjoner = journalpostDokumentInfoRelasjonRepository.findByDokumentInfoId(requestTo
				.getDokumentInfoId());
		validator.validateJournalpostDokumentInfoRelasjoner(jpDokInfoRelasjoner, requestTo);

		JournalpostDokumentInfoRelasjon jpDokInfoRelasjon = jpDokInfoRelasjoner.get(0);
		validator.validateJournalpostIdBelongsToThisJournalpost(jpDokInfoRelasjon.getJournalpost(), requestTo);
		validator.validateSletteStatusForDokument(jpDokInfoRelasjon.getDokumentInfo());

		log.info(REQUEST_ID + " sletter dokument med journalpostId={}, dokumentInfoId={}", requestTo.getJournalpostId(), requestTo
				.getDokumentInfoId());
		jpDokInfoRelasjon.getDokumentInfo().setSlettet(true);
		dokumentinfoRepository.save(jpDokInfoRelasjon.getDokumentInfo());

		return SlettDokumentResponseMapper.mapToSlettDokumentResponse(jpDokInfoRelasjon.getJournalpost(), jpDokInfoRelasjon.getDokumentInfo());
	}

}
