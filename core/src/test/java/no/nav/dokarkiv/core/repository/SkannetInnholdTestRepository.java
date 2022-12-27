package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.SkannetInnhold;

/**
 * Brukes kun til test
 */
public interface SkannetInnholdTestRepository extends HibernateRepository<SkannetInnhold>, BaseJpaTestRepository<SkannetInnhold, Long> {

}
