package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.Bruker;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BrukerRepository extends HibernateRepository<Bruker>, BaseJpaRepository<Bruker, Long> {
	@Modifying
	@Query(value = "DELETE from T_BRUKER WHERE JOURNALPOST_ID = :journalpostId", nativeQuery = true)
	void deleteBrukerByJournalpostId(@Param("journalpostId") String journalpostId);
}
