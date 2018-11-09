package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.Begrensning;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * @author Ketill Fenne, Visma Consulting
 */
public interface BegrensningRepository extends CrudRepository<Begrensning, Long> {
	@Query(value = "SELECT * FROM T_BEGRENSNING beg WHERE beg.DOKUMENT_INFO_ID=:dokumentInfoId and beg.journalpost_id is null and beg.begrensning_type = :begrensningType", nativeQuery = true)
    Optional<List<Begrensning>> findByDokumentInfoIdOnly(@Param("dokumentInfoId") Long dokumentInfoId, @Param("begrensningType") String begrensningType);

	@Query(value = "SELECT * FROM T_BEGRENSNING beg WHERE beg.DOKUMENT_INFO_ID=:dokumentInfoId and beg.journalpost_id = :journalpostId and beg.begrensning_type = :begrensningType", nativeQuery = true)
	Optional<List<Begrensning>> findByDokumentInfoIdJournalpostId(@Param("journalpostId") Long journalpostId, @Param("dokumentInfoId") Long dokumentInfoId, @Param("begrensningType") String begrensningType);

	@Query(value = "SELECT * FROM T_BEGRENSNING beg WHERE beg.DOKUMENT_INFO_ID is null and beg.journalpost_id = :journalpostId and beg.begrensning_type = :begrensningType", nativeQuery = true)
	Optional<List<Begrensning>> findByJournalpostIdOnly(@Param("journalpostId") Long journalpostId, @Param("begrensningType") String begrensningType);
}
