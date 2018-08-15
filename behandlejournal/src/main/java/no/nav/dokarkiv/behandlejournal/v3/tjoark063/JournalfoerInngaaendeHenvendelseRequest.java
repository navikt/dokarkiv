package no.nav.dokarkiv.behandlejournal.v3.tjoark063;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.ApplicationException;

/**
 * Domain request object for the JournalfoerInngaaendeHenvendelseMedHoveddokument service.
 *
 * @author Rune Romundstad, Visma Consulting
 */
@Builder
@Getter
@RequiredArgsConstructor
@ToString
public class JournalfoerInngaaendeHenvendelseRequest {

	private final Journalpost journalpost;

	/**
	 * Check that journalpost is set.
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
