package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.Journalpost;

import java.util.Optional;

public interface JournalpostTestRepository extends HibernateRepository<Journalpost>, BaseJpaTestRepository<Journalpost, Long> {
	Optional<Journalpost> findByKanalReferanseId(String kanalReferanseId);
}
