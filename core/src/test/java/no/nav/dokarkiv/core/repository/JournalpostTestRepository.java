package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JournalpostTestRepository extends HibernateRepository<Journalpost>, BaseJpaTestRepository<Journalpost, Long> {

	@Query(value = "SELECT JOURNALPOST_ID FROM t_jp_dok_info_rel j WHERE j.dokument_info_id = :dokumentInfoId", nativeQuery = true)
	List<Object> findAllJournalpostIdsByDokumentInfoId(@Param("dokumentInfoId") Long dokumentInfoId);

	Optional<Journalpost> findByKanalReferanseId(String kanalReferanseId);
}
