package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.Bruker;

public interface BrukerTestRepository extends HibernateRepository<Bruker>, BaseJpaTestRepository<Bruker, Long> {

}
