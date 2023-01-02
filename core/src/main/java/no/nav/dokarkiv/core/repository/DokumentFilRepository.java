package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.DokumentFil;

public interface DokumentFilRepository extends HibernateRepository<DokumentFil>, BaseJpaRepository<DokumentFil, Long> {
	DokumentFil findByFilUuid(String filUuid);
	void deleteByFilUuid(String filUuid);
}
