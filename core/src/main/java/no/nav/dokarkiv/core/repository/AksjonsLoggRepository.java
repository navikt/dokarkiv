package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;

public interface AksjonsLoggRepository extends HibernateRepository<AksjonsLogg>, BaseJpaRepository<AksjonsLogg, Long> {

}
