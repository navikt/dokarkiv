package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;

/**
 * Brukes kun til test
 */
public interface DokumentInfoTestRepository extends HibernateRepository<DokumentInfo>, BaseJpaTestRepository<DokumentInfo, Long> {

}

