package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.DokumentUrlInfo;

public interface DokumentUrlInfoRepository extends HibernateRepository<DokumentUrlInfo>, BaseJpaRepository<DokumentUrlInfo, Long> {
	DokumentUrlInfo findByFilUuid(String filUuid);
}
