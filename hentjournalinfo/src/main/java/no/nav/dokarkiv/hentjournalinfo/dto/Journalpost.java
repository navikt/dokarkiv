package no.nav.dokarkiv.hentjournalinfo.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Data
@Builder
public class Journalpost {

    private Long journalpostId;

    private JournalpostType journalpostType;

    private JournalpostStatus journalpostStatus;

    private String tema;

    private String tittel;

    private List<Bruker> brukere;

    private List<JournalpostDokumentRelasjon> knyttetDokumentList;

    @Data
    @Builder
    public static class Bruker {
        private String brukerId;
        private String brukerType;
    }

}
