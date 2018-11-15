package no.nav.dokarkiv.core.domain.service;

import static org.apache.commons.lang3.BooleanUtils.isFalse;

import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.entities.Begrensning;
import no.nav.dokarkiv.core.repository.BegrensningRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Transactional
@Component
public class BegrensningService {

	private final BegrensningRepository begrensningRepository;

	@Inject
	public BegrensningService(BegrensningRepository begrensningRepository) {
		this.begrensningRepository = begrensningRepository;
	}

	public boolean isJournalpostBegrenset(Long journalpostId, BegrensningTypeCode begrensningTypeCode) {
		Begrensning begrensning = begrensningRepository.findByJournalpostIdAndBegrensningTypeAndDokumentInfoIdIsNull(
				journalpostId, begrensningTypeCode).orElse(null);
		return isFalse(begrensning == null);
	}

    public boolean isJournalpostDokumentInfoRelasjonOrJournalpostBegrenset(Long journalpostId, Long dokumentInfoId, BegrensningTypeCode begrensningTypeCode) {
        return isJournalpostDokumentInfoRelasjonBegrenset(journalpostId, dokumentInfoId, begrensningTypeCode) || isJournalpostBegrenset(journalpostId, begrensningTypeCode);
    }

    public boolean isJournalpostDokumentInfoRelasjonBegrenset(Long journalpostId, Long dokumentInfoId, BegrensningTypeCode begrensningTypeCode) {
		Begrensning begrensning = begrensningRepository.findByJournalpostIdAndDokumentInfoIdAndBegrensningType(
				journalpostId, dokumentInfoId, begrensningTypeCode).orElse(null);
		return isFalse(begrensning == null);
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
}
