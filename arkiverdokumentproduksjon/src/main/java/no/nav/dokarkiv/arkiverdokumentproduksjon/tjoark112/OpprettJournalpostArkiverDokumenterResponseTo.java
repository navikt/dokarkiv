package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark112;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * The response object for the OpprettOgFerdigstillJournalpost service.
 *
 * @author Torgeir Cook.
 */
@Data
@AllArgsConstructor
@Builder
public class OpprettJournalpostArkiverDokumenterResponseTo {

	private Long journalpostId;
	private List<Long> dokumentInfoIdList;

}
