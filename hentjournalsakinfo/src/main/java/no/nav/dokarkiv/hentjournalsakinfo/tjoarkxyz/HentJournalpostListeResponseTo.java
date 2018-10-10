package no.nav.dokarkiv.hentjournalsakinfo.tjoarkxyz;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.dokarkiv.core.domain.entities.Journalpost;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Data
@NoArgsConstructor
@Builder
public class HentJournalpostListeResponseTo {

	private final List<Journalpost> gsakJournalpostList = new ArrayList<>();
	private final List<Journalpost> psakJournalpostList = new ArrayList<>();
}
