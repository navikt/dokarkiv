package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.Slettebestilling;

public interface SlettebestillingRepository extends HibernateRepository<Slettebestilling>, BaseJpaRepository<Slettebestilling, Long> {
}
