package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SaksrelasjonRepository extends CrudRepository<Saksrelasjon, Long> {
    @Query(value = "SELECT * FROM t_saksrelasjon rel WHERE rel.journalpost_id = :journalpostId", nativeQuery = true)
    Optional<Saksrelasjon> findSaksrelasjonByJournalpostId(@Param("journalpostId") String journalpostId);
}
