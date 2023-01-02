package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;

/**
 * Brukes kun til test
 */
public interface SaksrelasjonTestRepository extends HibernateRepository<Saksrelasjon>, BaseJpaTestRepository<Saksrelasjon, Long> {

}
