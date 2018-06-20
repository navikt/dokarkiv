package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface JoarkRepository extends CrudRepository<Journalpost, Long> {

	@Query(value = "select jt.journalpost_id from t_jp_tillegg jt where jt.nokkel = :nokkel and jt.verdi = :verdi", nativeQuery = true)
	Optional<Long> findJournalpostIdByTilleggsopplysningerNokkelAndVerdi(@Param("nokkel") String nokkel, @Param("verdi") String verdi);


}
