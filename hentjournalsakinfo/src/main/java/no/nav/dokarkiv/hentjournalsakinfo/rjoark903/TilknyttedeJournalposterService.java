package no.nav.dokarkiv.hentjournalsakinfo.rjoark903;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TilknyttedeJournalposterService {
	private final TilknyttedeJournalposterJdbcRepository repository;

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
