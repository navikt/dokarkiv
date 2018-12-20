package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Begrensning;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * @author Ketill Fenne, Visma Consulting
 */
public interface BegrensningRepository extends CrudRepository<Begrensning, Long> {
	Optional<Begrensning> findByJournalpostIdAndDokumentInfoIdAndBegrensningType(Long journalpostId, Long dokumentInfoId, BegrensningTypeCode begrensningTypeCode);

	Optional<Begrensning> findByJournalpostIdAndBegrensningTypeAndDokumentInfoIdIsNull(Long journalpostId, BegrensningTypeCode begrensningTypeCode);

	Optional<Begrensning> findByDokumentInfoIdAndVariantFormatAndBegrensningType(Long dokumentInfoId, VariantFormatCode variant, BegrensningTypeCode begrensningTypeCode);

	Optional<Begrensning> findByDokumentInfoIdAndBegrensningType(Long dokumentInfoId, BegrensningTypeCode begrensningTypeCode);

	void deleteByJournalpostIdAndBegrensningTypeAndDokumentInfoIdIsNull(Long journalpostId, BegrensningTypeCode begrensningTypeCode);

	void deleteByJournalpostIdAndDokumentInfoIdAndBegrensningType(Long journalpostId, Long dokumentInfoId, BegrensningTypeCode begrensningTypeCode);

	void deleteByDokumentInfoIdAndBegrensningType(Long dokumentInfoId, BegrensningTypeCode begrensningTypeCode);

}
