package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface JournalpostDokumentInfoRelasjonRepository extends CrudRepository<JournalpostDokumentInfoRelasjon, Long> {
	@Query(value = "SELECT * FROM T_JP_DOK_INFO_REL dir WHERE dir.DOKUMENT_INFO_ID=:dokumentInfoId", nativeQuery = true)
    Optional<List<JournalpostDokumentInfoRelasjon>> findByDokumentInfoId(@Param("dokumentInfoId") Long dokumentInfoId);
}
