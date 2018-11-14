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
import java.util.Optional;

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
		List<Begrensning> begrensning = begrensningRepository.findAllByJournalpostIdAndBegrensningTypeAndDokumentInfoIdIsNull(
				journalpostId, begrensningTypeCode).orElse(new ArrayList<>());
		return isFalse(begrensning.isEmpty());
	}

    public boolean isDokumentInfoBegrenset(Long dokumentInfoId, BegrensningTypeCode begrensningTypeCode) {
        Optional<Begrensning> begrensning = begrensningRepository.findByDokumentInfoIdAndBegrensningTypeAndJournalpostIdIsNull(
                dokumentInfoId, begrensningTypeCode);
        return begrensning.isPresent();
    }

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
		Begrensning begrensning = begrensningRepository.findByJournalpostIdAndBegrensningTypeAndDokumentInfoIdIsNull(
				journalpostId, begrensningTypeCode);
		begrensningRepository.delete(begrensning);
	}

	public void deleteValidertJournalpostDokumentInfoRelasjonBegrensning(
			Long journalpostId, Long dokumentInfoId, BegrensningTypeCode begrensningTypeCode) {
		Begrensning begrensning = begrensningRepository.findByJournalpostIdAndDokumentInfoIdAndBegrensningType(
				journalpostId, dokumentInfoId, begrensningTypeCode);
		begrensningRepository.delete(begrensning);
	}

}
