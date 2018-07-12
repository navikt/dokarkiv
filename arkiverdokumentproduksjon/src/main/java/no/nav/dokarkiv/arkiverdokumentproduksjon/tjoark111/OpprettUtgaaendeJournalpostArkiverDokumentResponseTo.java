package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;

import java.util.ArrayList;
import java.util.List;

/**
 * The response object for the OpprettOgFerdigstillJournalpost service.
 *
 * @author Torgeir Cook.
 */
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class OpprettUtgaaendeJournalpostArkiverDokumentResponseTo {

	private Long journalpostId;
	private Long dokumentInfoIdHoveddokument;

	@Builder.Default
	private List<Long> dokumentInfoIdVedlegg = new ArrayList<>();

	private JournalStatusCode journalStatus;
}
