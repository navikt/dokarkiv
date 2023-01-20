package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;

public interface AksjonsLoggTestRepository extends HibernateRepository<AksjonsLogg>, BaseJpaTestRepository<AksjonsLogg, Long> {

}
