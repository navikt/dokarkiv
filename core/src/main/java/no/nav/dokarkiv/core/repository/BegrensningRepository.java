package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.entities.Begrensning;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

/**
 * @author Ketill Fenne, Visma Consulting
 */
public interface BegrensningRepository extends CrudRepository<Begrensning, Long> {
	Optional<List<Begrensning>> findAllByJournalpostIdAndDokumentInfoIdAndBegrensningType(Long journalpostId, Long dokumentInfoId, BegrensningTypeCode begrensningTypeCode);

	Begrensning findByJournalpostIdAndDokumentInfoIdAndBegrensningType(Long journalpostId, Long dokumentInfoId, BegrensningTypeCode begrensningTypeCode);

	Optional<List<Begrensning>> findAllByJournalpostIdAndBegrensningTypeAndDokumentInfoIdIsNull(Long journalpostId, BegrensningTypeCode begrensningTypeCode);

	Begrensning findByJournalpostIdAndBegrensningTypeAndDokumentInfoIdIsNull(Long journalpostId, BegrensningTypeCode begrensningTypeCode);

    Optional<Begrensning> findByDokumentInfoIdAndBegrensningTypeAndJournalpostIdIsNull(Long dokumentInfoId, BegrensningTypeCode begrensningTypeCode);
}
