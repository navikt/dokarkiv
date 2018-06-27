package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark109;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Thomas Kåsene, Visma Consulting AS
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class KnyttDokumentTilJournalpostSomVedleggRequestTo {

	private long knyttesFraJournalpostId;
	private long knyttesTilJournalpostId;
	private long dokumentInfoId;
	private String endretAvNavn;

}
