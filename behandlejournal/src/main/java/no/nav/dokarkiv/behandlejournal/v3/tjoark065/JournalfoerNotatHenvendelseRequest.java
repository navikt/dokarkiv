package no.nav.dokarkiv.behandlejournal.v3.tjoark065;

import lombok.Data;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.ApplicationException;

/**
 * The request object for the JournalfoerNotatHenvendelse service.
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@Data
public class JournalfoerNotatHenvendelseRequest {
	private final Journalpost journalpost;

	/**
	 * Validate that journalpost with id is set.
	 */
	public void validate() {
		if (journalpost == null) {
			throw new ApplicationException("Missing parameter in request: Journalpost");
		}

		if (journalpost.findHoveddokumentDokumentInfoRelasjon() == null) {
			throw new ApplicationException("Missing parameter in request: Hoveddokument");
		}
	}
}
