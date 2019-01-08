package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.Hendelselogg;
import org.springframework.data.repository.CrudRepository;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public interface Hendelseloggrepository extends CrudRepository<Hendelselogg, Long> {

}
