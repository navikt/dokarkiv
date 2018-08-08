package no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark067;

import lombok.Builder;
import lombok.Value;
import no.nav.dokarkiv.core.exceptions.UgyldigInputException;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Builder
@Value
public class FerdigstillJournalfoeringTo {
	private final String journalpostId;
	private final String enhetId;

	public void validate() {
		notNull(journalpostId, "journalpostId");
		notNull(enhetId, "enhetId");
	}

	private void notNull(Object object, String field) {
		if (object == null) {
			throw new UgyldigInputException("field=" + field + " must be set. journalpostId=" + journalpostId);
		}
	}
}
