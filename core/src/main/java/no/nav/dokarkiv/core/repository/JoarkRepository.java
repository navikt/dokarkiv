package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.Journalpost;
import org.springframework.data.repository.CrudRepository;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface JoarkRepository extends CrudRepository<Journalpost, Long> {


	Long findJournalpostByTilleggsopplysningerContainingNokkelAnd(String nokkel, String verdi);
}
