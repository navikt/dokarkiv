package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;

import java.util.List;

public interface JournalpostDokumentInfoRelasjonTestRepository extends HibernateRepository<JournalpostDokumentInfoRelasjon>, BaseJpaTestRepository<JournalpostDokumentInfoRelasjon, Long>, CustomJournalpostDokumentInfoRelasjonRepository {
	List<JournalpostDokumentInfoRelasjon> findAllByJournalpostJournalpostId(Long journalpostId);

	List<JournalpostDokumentInfoRelasjon> findAllByDokumentInfoDokumentInfoId(Long dokumentInfoId);
}
