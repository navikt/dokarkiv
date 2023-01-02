package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;

import java.util.List;

public interface DokumentInfoRepository extends HibernateRepository<DokumentInfo>, BaseJpaRepository<DokumentInfo, Long>, CustomDokumentInfoRepository {
	List<DokumentInfo> findByOriginalJournalpostJournalpostId(Long journalpostId);
}

