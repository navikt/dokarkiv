package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.repository.projections.IdHolder;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

/**
 * Utvidelse for {@link DokumentInfoRepository} for kode i test og prod.
 */
@NoRepositoryBean
public interface CustomDokumentInfoRepository extends Repository<DokumentInfo, Long> {
	@Query("""
			select new no.nav.dokarkiv.core.repository.projections.IdHolder(
			di.originalJournalpost.journalpostId
			)
			from DokumentInfo di
			where di.dokumentInfoId = :dokumentInfoId
			""")
	IdHolder findOriginalJournalpostIdByDokumentInfoId(Long dokumentInfoId);
}
