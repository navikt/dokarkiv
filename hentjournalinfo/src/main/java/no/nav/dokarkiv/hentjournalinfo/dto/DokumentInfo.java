package no.nav.dokarkiv.hentjournalinfo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.dokarkiv.hentjournalinfo.dto.kode.DokumentStatus;
import no.nav.dokarkiv.hentjournalinfo.dto.kode.FilType;
import no.nav.dokarkiv.hentjournalinfo.dto.kode.VariantFormat;

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

    private DokumentStatus dokumentStatus;

    private Journalpost originalJournalpost;

    private List<JournalpostDokumentRelasjon> knyttetJournalpostList;

    private List<Fildetaljer> filDetaljerList;

    private Boolean kassert;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Fildetaljer {
        private Long fildetaljerId;
        private FilType filtype;
        private VariantFormat variantFormat;
        private boolean skjermet;
    }

}
