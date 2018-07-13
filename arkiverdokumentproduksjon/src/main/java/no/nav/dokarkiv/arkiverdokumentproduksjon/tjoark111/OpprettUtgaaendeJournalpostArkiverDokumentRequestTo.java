package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.dokarkiv.core.domain.entities.Journalpost;

import java.util.ArrayList;
import java.util.List;

/**
 * Request objekt for OpprettUtgaaendeJournalpostArkiverDokument.
 *
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpprettUtgaaendeJournalpostArkiverDokumentRequestTo {
	private Journalpost journalpost;
	private boolean forsokFerdigstilling;
	private String journalforendeEnhet;
	@Builder.Default
	private List<Vedlegg> vedleggList = new ArrayList<>();

	@Builder
	@Data
	@AllArgsConstructor
	public static final class Vedlegg {
		private Long knyttesFraJournalpostId;
		private Long dokumentInfoId;
	}
}
