package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;

import java.util.List;
import java.util.Optional;

/**
 * Brukes kun til test
 */
public interface DokumentInfoTestRepository extends HibernateRepository<DokumentInfo>, BaseJpaTestRepository<DokumentInfo, Long> {
	Optional<DokumentInfo> findByDokumentInfoId(Long dokumentInfoId);

	List<DokumentInfo> findByOriginalJournalpostJournalpostId(Long journalpostId);
}

