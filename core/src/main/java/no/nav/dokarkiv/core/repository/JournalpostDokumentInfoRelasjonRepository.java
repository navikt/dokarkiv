package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface JournalpostDokumentInfoRelasjonRepository extends CrudRepository<JournalpostDokumentInfoRelasjon, Long> {
	@Query(value = "SELECT * FROM T_JP_DOK_INFO_REL dir WHERE dir.DOKUMENT_INFO_ID=:dokumentInfoId", nativeQuery = true)
	List<JournalpostDokumentInfoRelasjon> findByDokumentInfoId(@Param("dokumentInfoId") Long dokumentInfoId);

	@Query(value = "SELECT * FROM T_JP_DOK_INFO_REL dir WHERE dir.JOURNALPOST_ID=:journalpostId", nativeQuery = true)
	List<JournalpostDokumentInfoRelasjon> findByJournalpostId(@Param("journalpostId") Long journalpostId);

	@Query(value = "SELECT dir.DOKUMENT_INFO_ID FROM T_JP_DOK_INFO_REL dir WHERE dir.JOURNALPOST_ID=:journalpostId", nativeQuery = true)
	Long findDokumentInfoIdByJournalpostId(@Param("journalpostId") Long journalpostId);

}
