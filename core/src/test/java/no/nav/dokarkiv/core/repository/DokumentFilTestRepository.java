package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.DokumentFil;

public interface DokumentFilTestRepository extends HibernateRepository<DokumentFil>, BaseJpaTestRepository<DokumentFil, Long> {
	DokumentFil findByFilUuid(String filUuid);
	void deleteByFilUuid(String filUuid);
}
