package no.nav.dokarkiv.hentjournalsakinfo.rjoark903;

import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class TilknyttedeJournalposterService {
	private final TilknyttedeJournalposterJdbcRepository repository;

	@Inject
	public TilknyttedeJournalposterService(TilknyttedeJournalposterJdbcRepository repository) {
		this.repository = repository;
	}

	public TilknyttedeJournalposterResponse tilknyttedeJournalposter(Long dokumentInfoId, Tilknytning tilknytning) {
		return new TilknyttedeJournalposterResponse(doTilknyttedeJournalposter(dokumentInfoId, tilknytning));
	}

	private List<TilknyttetJournalpostDto> doTilknyttedeJournalposter(Long dokumentInfoId, Tilknytning tilknytning) {
		switch (tilknytning) {
			case GJENBRUK:
				return repository.findGjenbrukteJournalposter(dokumentInfoId);
			case SPLITT:
				return repository.findSplittedeJournalposter(dokumentInfoId);
			default:
				return repository.findGjenbrukteJournalposter(dokumentInfoId);
		}
	}
}
