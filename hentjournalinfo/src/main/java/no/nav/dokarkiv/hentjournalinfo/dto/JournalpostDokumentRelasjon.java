package no.nav.dokarkiv.hentjournalinfo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.dokarkiv.hentjournalinfo.dto.kode.TilknyttetJournalpostSom;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalpostDokumentRelasjon {

    private Long dokumentInfoId;
    private Long journalpostId;

    private TilknyttetJournalpostSom tilknyttetJournalpostSom;
    private Boolean slettet;
    private Journalpost journalpost;
    private DokumentInfo dokumentInfo;

}
