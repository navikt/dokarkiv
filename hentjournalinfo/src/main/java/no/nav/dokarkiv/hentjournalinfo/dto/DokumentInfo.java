package no.nav.dokarkiv.hentjournalinfo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DokumentInfo {

    private Long dokumentInfoId;

    private String tittel;

    private Map<String, String> tilleggsopplysninger;

    private String status;

    private Journalpost originalJournalpost;

    private List<JournalpostDokumentRelasjon> knyttetJournalpostList;

    private List<Fildetaljer> filDetaljerList;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Fildetaljer {
        private Long fildetaljerId;
        private String filtype;
        private String variantFormat;
    }

}
