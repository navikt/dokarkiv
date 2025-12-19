package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.Slettebestilling;

public interface SlettebestillingTestRepository extends HibernateRepository<Slettebestilling>, BaseJpaTestRepository<Slettebestilling, Long> {
}
