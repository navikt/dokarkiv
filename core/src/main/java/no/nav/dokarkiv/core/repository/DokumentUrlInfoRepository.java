package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.DokumentUrlInfo;
import org.springframework.data.repository.CrudRepository;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface DokumentUrlInfoRepository extends CrudRepository<DokumentUrlInfo, Long> {
	DokumentUrlInfo findByFilUuid(String filUuid);
}
