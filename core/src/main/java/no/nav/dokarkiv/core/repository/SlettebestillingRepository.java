package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.Slettebestilling;

import java.util.List;

public interface SlettebestillingRepository extends HibernateRepository<Slettebestilling>, BaseJpaRepository<Slettebestilling, Long> {
	List<Slettebestilling> findByDokumentInfoId(Long dokumentInfoId);
}
