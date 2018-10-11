package no.nav.dokarkiv.hentjournalsakinfo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalpostDokumentInfoRelasjon {

	private Long journalpostDokumentInfoRelasjonId;
	private String tilknyttetAvNavn;
	private TilknyttetJournalpostSomCode tilknyttetJournalpostSom;
	private DokumentInfo dokumentInfo;

}
