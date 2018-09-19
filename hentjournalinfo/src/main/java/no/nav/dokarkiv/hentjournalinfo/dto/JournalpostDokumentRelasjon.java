package no.nav.dokarkiv.hentjournalinfo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    private String tilknyttetJournalpostSom;
    private Journalpost journalpost;
    private DokumentInfo dokumentInfo;

}
