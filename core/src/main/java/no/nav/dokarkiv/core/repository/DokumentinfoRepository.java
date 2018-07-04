package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface DokumentinfoRepository extends CrudRepository<DokumentInfo, Long> {
}
