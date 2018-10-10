package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.Bruker;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface BrukerRepository extends CrudRepository<Bruker, Long> {

	@Modifying
	@Query(value = "DELETE from T_BRUKER WHERE JOURNALPOST_ID = :journalpostId", nativeQuery = true)
	void deleteBrukerByJournalpostId(@Param("journalpostId") String journalpostId);

}
