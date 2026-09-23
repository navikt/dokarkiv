package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.FilDetaljer;

/// Brukes kun til test
public interface FilDetaljerTestRepository extends HibernateRepository<FilDetaljer>, BaseJpaTestRepository<FilDetaljer, Long> {

	FilDetaljer findByFilUuid(String arkivFilUuid);
}