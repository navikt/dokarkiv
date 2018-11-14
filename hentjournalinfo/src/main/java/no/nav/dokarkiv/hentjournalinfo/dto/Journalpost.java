package no.nav.dokarkiv.hentjournalinfo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.hentjournalinfo.dto.kode.BrukerType;
import no.nav.dokarkiv.hentjournalinfo.dto.kode.JournalpostStatus;
import no.nav.dokarkiv.hentjournalinfo.dto.kode.JournalpostType;
import no.nav.dokarkiv.hentjournalinfo.dto.kode.Tema;

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

    private Tema tema;

    private String tittel;

    private List<Bruker> brukere;

    private Saksrelasjon saksrelasjon;

    private List<JournalpostDokumentRelasjon> knyttetDokumentList;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Bruker {
        private String brukerId;
        private BrukerType brukerType;
    }

}
