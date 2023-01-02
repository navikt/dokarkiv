package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.repository.projections.IdHolder;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface JournalpostDokumentInfoRelasjonTestRepository extends HibernateRepository<JournalpostDokumentInfoRelasjon>, BaseJpaTestRepository<JournalpostDokumentInfoRelasjon, Long>, CustomJournalpostDokumentInfoRelasjonRepository {
	List<JournalpostDokumentInfoRelasjon> findAllByJournalpostJournalpostId(Long journalpostId);

	List<JournalpostDokumentInfoRelasjon> findAllByDokumentInfoDokumentInfoId(Long dokumentInfoId);

	@Query("""
			select new no.nav.dokarkiv.core.repository.projections.IdHolder(
			jdr.journalpost.journalpostId
			)
			from JournalpostDokumentInfoRelasjon jdr
			where jdr.dokumentInfo.dokumentInfoId = :dokumentInfoId
			""")
	List<IdHolder> findAllJournalpostIdsByDokumentInfoId(Long dokumentInfoId);
}
