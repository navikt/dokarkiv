package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.Sak;

public interface SakRepository extends HibernateRepository<Sak>, BaseJpaRepository<Sak, Long> {

}
