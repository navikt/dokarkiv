package no.nav.dokarkiv.behandlejournal.v3.tjoark060;

import lombok.Data;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.ApplicationException;

/**
 * Request object for the ArkiverUstrukturertKrav service.
 * 
 * @author Rune Romundstad, Visma Consulting
 *
 */
@Data
public class ArkiverUstrukturertKravRequest {

	private final Journalpost journalpost;

	/**
	 * Validate that a Journalpost is set in the request.
	 */
	public void validate() {
		if (journalpost == null) {
			throw new ApplicationException("Journalpost must be set");
		}
	}
}
