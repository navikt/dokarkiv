package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101;

import lombok.Builder;
import lombok.Data;

/**
 * The response object for the OpprettJournalpostService as a part in
 * arkiverDokumentproduksjon
 *
 * @author Stig Strøm
 */
@Data
@Builder
public class OpprettJournalpostResponseTo {
	private final Long journalpostId;
	private final Long dokumentInfoId;
}
