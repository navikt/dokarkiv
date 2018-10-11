package no.nav.dokarkiv.hentjournalsakinfo.tjoarkxyz;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.dokarkiv.hentjournalsakinfo.dto.Journalpost;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HentJournalpostListeResponseTo {

	@Builder.Default
	private final List<Journalpost> gsakJournalpostList = new ArrayList<>();
	@Builder.Default
	private final List<Journalpost> psakJournalpostList = new ArrayList<>();
}
