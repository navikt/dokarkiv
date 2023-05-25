package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;

import java.util.List;

public interface AksjonsLoggTestRepository extends HibernateRepository<AksjonsLogg>, BaseJpaTestRepository<AksjonsLogg, Long> {

	List<AksjonsLogg> getAksjonsLoggByJournalpostId(Long journalpostId);

}
