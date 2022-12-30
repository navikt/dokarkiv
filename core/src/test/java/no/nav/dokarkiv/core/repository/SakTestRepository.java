package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.Sak;

public interface SakTestRepository extends HibernateRepository<Sak>, BaseJpaTestRepository<Sak, Long> {

}
