package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;

import java.util.List;
import java.util.Optional;

public interface DokumentInfoRepository extends HibernateRepository<DokumentInfo>, BaseJpaRepository<DokumentInfo, Long> {
	Optional<DokumentInfo> findByDokumentInfoId(Long dokumentInfoId);

	List<DokumentInfo> findByOriginalJournalpostJournalpostId(Long journalpostId);
}

