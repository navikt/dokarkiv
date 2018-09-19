package no.nav.dokarkiv.hentjournalinfo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Bruker {
        private String brukerId;
        private String brukerType;
    }

}
