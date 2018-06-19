package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.Tilleggsopplysninger;
import org.springframework.data.repository.CrudRepository;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public interface TilleggsopplysningerRepository extends CrudRepository<Tilleggsopplysninger, Long> {

	Long findJournalpost_IdByNokkelAndVerdi(String nokkel, String verdi);
}
