package no.nav.dokarkiv.hentjournalinfo.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Data
@Builder
public class JournalpostDokumentRelasjon {

    private Long dokumentInfoId;
    private Long journalpostId;

    private String tilknyttetJournalpostSom;
    private Journalpost journalpost;
    private DokumentInfo dokumentInfo;

}
