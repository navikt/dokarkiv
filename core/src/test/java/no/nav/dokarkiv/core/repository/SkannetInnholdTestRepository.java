package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.SkannetInnhold;

import java.util.List;

/**
 * Brukes kun til test
 */
public interface SkannetInnholdTestRepository extends HibernateRepository<SkannetInnhold>, BaseJpaTestRepository<SkannetInnhold, Long> {
	List<SkannetInnhold> findAllByDokumentInfo(DokumentInfo dokumentInfo);
}
