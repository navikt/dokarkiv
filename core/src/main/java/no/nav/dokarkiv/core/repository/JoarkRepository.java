package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.springframework.data.repository.CrudRepository;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface JoarkRepository extends CrudRepository<Journalpost, Long> {


}
