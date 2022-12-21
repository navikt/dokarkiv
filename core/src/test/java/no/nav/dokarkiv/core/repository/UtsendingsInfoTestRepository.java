package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.UtsendingsInfo;

/**
 * Brukes kun til test
 */
public interface UtsendingsInfoTestRepository extends HibernateRepository<UtsendingsInfo>, BaseJpaTestRepository<UtsendingsInfo, Long> {

}
