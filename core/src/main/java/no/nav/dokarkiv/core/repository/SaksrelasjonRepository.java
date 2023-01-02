package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;

import java.util.Optional;

public interface SaksrelasjonRepository extends HibernateRepository<Saksrelasjon>, BaseJpaRepository<Saksrelasjon, Long> {
    Optional<Saksrelasjon> findByJournalpostId(Long journalpostId);
}
