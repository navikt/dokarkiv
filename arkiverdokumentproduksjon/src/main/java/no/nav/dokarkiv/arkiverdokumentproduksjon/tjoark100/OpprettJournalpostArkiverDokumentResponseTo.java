package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark100;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * The response object for the OpprettOgFerdigstillJournalpost service.
 *
 * @author Torgeir Cook.
 */
@Data
@AllArgsConstructor
public class OpprettJournalpostArkiverDokumentResponseTo {

	private Long journalpostId;
	private Long dokumentInfoId;

}
