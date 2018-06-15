package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.DokumentFil;
import org.springframework.data.repository.Repository;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface DokumentFilRepository extends Repository<DokumentFil, String> {
	DokumentFil findByFilUuid(String filUuid);
}
