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
	private final Long journalpostId;
	private List<DokumentInfoIdEntryTo> dokumentInfoIds;


	@Builder
	@Data
	static class DokumentInfoIdEntryTo {
		private final String filreferanse;
		private final long dokumentInfoId;

		@Override
		public String toString() {
			return "(" + filreferanse + "," + dokumentInfoId + ")";
		}
	}
}
