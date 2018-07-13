package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;

import java.util.ArrayList;
import java.util.List;

/**
 * Respons objekt for OpprettUtgaaendeJournalpostArkiverDokument service.
 *
 * @author Ugur Alpay Cenar, Visma Consulting.
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
