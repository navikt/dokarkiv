package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.codes.SakStatus;

public interface SakStatusTestRepository extends HibernateRepository<SakStatus>, BaseJpaTestRepository<SakStatus, Long> {
}