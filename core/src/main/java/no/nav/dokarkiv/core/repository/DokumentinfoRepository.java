package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface DokumentinfoRepository extends CrudRepository<DokumentInfo, Long> {

//	@Query(value = "SELECT * FROM T_DOKUMENT_INFO jt WHERE jt.DOKUMENT_INFO_ID = :dokumentinfoId and jt.ORIG_JOURNALPOST_ID = :originalJournalpostId", nativeQuery = true)
//	Optional<DokumentInfo> findDokumentInfoByJournalpostIdAndDokumentInfoId(@Param("originalJournalpostId") Long originalJournalpostId, @Param("dokumentinfoId") Long dokumentinfoId);

	Optional<DokumentInfo> findAllByOriginalJournalpostJournalpostIdAndDokumentInfoId(Long journalpostId, Long dokumentInfoId);


}

