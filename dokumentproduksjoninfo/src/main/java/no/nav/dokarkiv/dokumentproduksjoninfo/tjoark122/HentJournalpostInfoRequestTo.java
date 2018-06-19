package no.nav.dokarkiv.dokumentproduksjoninfo.tjoark122;

import lombok.Builder;
import lombok.Data;
import no.nav.dokarkiv.dokumentproduksjoninfo.exceptions.NoJournalpostFoundException;

/**
 * Internal DTO for HentJournalpostInfoService requests
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@Builder
public class HentJournalpostInfoRequestTo {
	private Long journalpostId;
	private Long dokumentInfoId;

	public void validate() throws NoJournalpostFoundException {
		if (journalpostId == null || journalpostId == 0L) {
			throw new NoJournalpostFoundException("Missing parameter journalpostId", journalpostId);
		}
	}
}
