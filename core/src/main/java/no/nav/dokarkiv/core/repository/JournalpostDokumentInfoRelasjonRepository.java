package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.List;

public interface JournalpostDokumentInfoRelasjonRepository extends HibernateRepository<JournalpostDokumentInfoRelasjon>, BaseJpaRepository<JournalpostDokumentInfoRelasjon, Long>, CustomJournalpostDokumentInfoRelasjonRepository {

	/**
	 * Sletter en entitet.
	 *
	 * @param entitet kan ikke være {@literal null}.
	 * @throws IllegalArgumentException hvis entitet {@literal null}.
	 * @throws OptimisticLockingFailureException when the entity uses optimistic locking and has a version attribute with
	 *           a different value from that found in the persistence store. Also thrown if the entity is assumed to be
	 *           present but does not exist in the database.
	 */
	void delete(JournalpostDokumentInfoRelasjon entitet);

	List<JournalpostDokumentInfoRelasjon> findAllByJournalpostJournalpostId(Long journalpostId);

	List<JournalpostDokumentInfoRelasjon> findAllByDokumentInfoDokumentInfoId(Long dokumentInfoId);
}
