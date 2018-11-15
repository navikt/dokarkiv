package no.nav.dokarkiv.core.domain.service;

import static org.apache.commons.lang3.BooleanUtils.isFalse;

import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.entities.Begrensning;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.repository.BegrensningRepository;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Transactional
@Component
public class BegrensningService {

	private final BegrensningRepository begrensningRepository;
	private final JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;

	@Inject
	public BegrensningService(BegrensningRepository begrensningRepository, JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository) {
		this.begrensningRepository = begrensningRepository;
		this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
	}

	public boolean isJournalpostBegrenset(Long journalpostId, BegrensningTypeCode begrensningTypeCode) {
		Begrensning begrensning = begrensningRepository.findByJournalpostIdAndBegrensningTypeAndDokumentInfoIdIsNull(
				journalpostId, begrensningTypeCode).orElse(null);
		return isFalse(begrensning == null);
	}

    public boolean isDokumentInfoBegrenset(Long dokumentInfoId, BegrensningTypeCode begrensningTypeCode) {
        Optional<Begrensning> begrensning = begrensningRepository.findByDokumentInfoIdAndBegrensningTypeAndJournalpostIdIsNull(
                dokumentInfoId, begrensningTypeCode);
        return begrensning.isPresent();
    }

    public boolean isJournalpostDokumentInfoRelasjonOrJournalpostBegrenset(Long journalpostId, Long dokumentInfoId, BegrensningTypeCode begrensningTypeCode) {
        return isJournalpostDokumentInfoRelasjonBegrenset(journalpostId, dokumentInfoId, begrensningTypeCode) || isJournalpostBegrenset(journalpostId, begrensningTypeCode);
    }

    public boolean isJournalpostDokumentInfoRelasjonBegrenset(Long journalpostId, Long dokumentInfoId, BegrensningTypeCode begrensningTypeCode) {
		Begrensning begrensning = begrensningRepository.findByJournalpostIdAndDokumentInfoIdAndBegrensningType(
				journalpostId, dokumentInfoId, begrensningTypeCode).orElse(null);
		return isFalse(begrensning == null);

    public boolean isJournalpostDokumentInfoRelasjonBegrenset(
			Long journalpostId,
			Long dokumentInfoId,
			BegrensningTypeCode begrensningTypeCode) {
		List<Begrensning> begrensning = begrensningRepository.findAllByJournalpostIdAndDokumentInfoIdAndBegrensningType(
				journalpostId, dokumentInfoId, begrensningTypeCode).orElse(new ArrayList<>());
		return isFalse(begrensning.isEmpty());
	}

	public void saveBegrensning(Begrensning begrensning) {
		begrensningRepository.save(begrensning);
	}

	public void deleteValidertJournalpostBegrensning(
			Long journalpostId,
			BegrensningTypeCode begrensningTypeCode) {
		begrensningRepository.deleteByJournalpostIdAndBegrensningTypeAndDokumentInfoIdIsNull(journalpostId, begrensningTypeCode);
//		Begrensning begrensning = begrensningRepository.findByJournalpostIdAndBegrensningTypeAndDokumentInfoIdIsNull(
//				journalpostId, begrensningTypeCode);
//		begrensningRepository.delete(begrensning);
	}

	public void deleteValidertJournalpostDokumentInfoRelasjonBegrensning(
			Long journalpostId, Long dokumentInfoId, BegrensningTypeCode begrensningTypeCode) {
//		Begrensning begrensning = begrensningRepository.findByJournalpostIdAndDokumentInfoIdAndBegrensningType(
//				journalpostId, dokumentInfoId, begrensningTypeCode);
//		begrensningRepository.delete(begrensning);
		begrensningRepository.deleteByJournalpostIdAndDokumentInfoIdAndBegrensningType(journalpostId, dokumentInfoId, begrensningTypeCode);
	}


	public Journalpost addBegrensetRelasjonerToJournalpost(Journalpost journalpost) {
		List<Long> begrensetDokumentInfoIds = journalpostDokumentInfoRelasjonRepository.findBegrensetRelasjonDokumentInfoIdByJournalpostId(journalpost
				.getJournalpostId()).orElseGet(ArrayList::new).stream().map(BigInteger::longValue).collect(Collectors.toList());
		journalpost.addAllbegrensetRelasjonerDokumentInfoIds(begrensetDokumentInfoIds);
		return journalpost;
	}

}
