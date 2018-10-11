package no.nav.dokarkiv.hentjournalsakinfo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Data
@Builder
@AllArgsConstructor
public class JournalpostDokumentInfoRelasjon {

	private final Long journalpostDokumentInfoRelasjonId;
	private final String tilknyttetAvNavn;
	private final TilknyttetJournalpostSomCode tilknyttetJournalpostSom;
	private final DokumentInfo dokumentInfo;

}
