package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import org.springframework.data.repository.CrudRepository;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public interface AksjonsLoggRepository extends CrudRepository<AksjonsLogg, Long> {

}
