package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import org.springframework.data.repository.CrudRepository;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface DokumentFilRepository extends CrudRepository<DokumentFil, String> {
	DokumentFil findByFilUuid(String filUuid);
	void deleteByFilUuid(String filUuid);
}
