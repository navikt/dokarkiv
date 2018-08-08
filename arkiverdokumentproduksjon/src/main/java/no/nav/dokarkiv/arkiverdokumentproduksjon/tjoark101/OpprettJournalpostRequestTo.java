package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.ApplicationException;

/**
 * Request object for operation OpprettJournalpost method in the
 * arkiverDokumentproduksjon
 *
 * @author Stig Strøm
 */
@RequiredArgsConstructor
@Getter
@ToString
public class OpprettJournalpostRequestTo {
	private final Journalpost journalpost;

	public void validate() {
		if (journalpost == null) {
			throw new ApplicationException("Missing parameter in request: Journalpost");
		}

		if (journalpost.findHoveddokumentDokumentInfoRelasjon() == null) {
			throw new ApplicationException("Missing parameter in request: Hoveddokument");
		}
	}
}
