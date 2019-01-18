package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Begrensning;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * @author Ketill Fenne, Visma Consulting
 */
public interface BegrensningRepository extends CrudRepository<Begrensning, Long> {
	Optional<Begrensning> findByJournalpostIdAndDokumentInfoIdAndBegrensningType(Long journalpostId, Long dokumentInfoId, SkjermingTypeCode skjermingTypeCode);

	Optional<Begrensning> findByJournalpostIdAndBegrensningTypeAndDokumentInfoIdIsNull(Long journalpostId, SkjermingTypeCode skjermingTypeCode);

	Optional<Begrensning> findByDokumentInfoIdAndVariantFormatAndBegrensningType(Long dokumentInfoId, VariantFormatCode variant, SkjermingTypeCode skjermingTypeCode);

	Optional<Begrensning> findByDokumentInfoIdAndBegrensningType(Long dokumentInfoId, SkjermingTypeCode skjermingTypeCode);

	void deleteByJournalpostIdAndBegrensningTypeAndDokumentInfoIdIsNull(Long journalpostId, SkjermingTypeCode skjermingTypeCode);

	void deleteByJournalpostIdAndDokumentInfoIdAndBegrensningType(Long journalpostId, Long dokumentInfoId, SkjermingTypeCode skjermingTypeCode);

	void deleteByDokumentInfoIdAndBegrensningType(Long dokumentInfoId, SkjermingTypeCode skjermingTypeCode);

}
