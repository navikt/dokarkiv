package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.codes.Innsyn;

public interface InnsynTestRepository extends HibernateRepository<Innsyn>, BaseJpaTestRepository<Innsyn, String> {

}
