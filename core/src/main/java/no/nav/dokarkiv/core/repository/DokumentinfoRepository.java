package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface DokumentinfoRepository extends CrudRepository<DokumentInfo, Long> {
	Optional<DokumentInfo> findByDokumentInfoId(Long dokumentInfoId);

	List<DokumentInfo> findByOriginalJournalpostJournalpostId(Long journalpostId);
}

