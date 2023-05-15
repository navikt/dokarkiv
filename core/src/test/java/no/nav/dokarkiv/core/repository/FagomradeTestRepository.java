package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.codes.Fagomrade;

public interface FagomradeTestRepository extends HibernateRepository<Fagomrade>, BaseJpaTestRepository<Fagomrade, Long> {
}