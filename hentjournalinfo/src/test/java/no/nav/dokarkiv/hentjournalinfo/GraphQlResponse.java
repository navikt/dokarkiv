package no.nav.dokarkiv.hentjournalinfo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Example response object for dokarkiv graphql response
 * You should define the variables you need for the request
 *
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Data
@Builder
public class GraphQlResponse {

    private ResponseData data;

    @Data
    @Builder
    public static class ResponseData {
        private String fil;
        private Journalpost journalpost;
        private DokumentInfo dokumentInfo;
    }

    @Data
    @Builder
    public static class Journalpost {
        private String tema;
        private List<Bruker> brukere;
        private List<Kryssreferanse> kryssreferanser;
        private List<Tilleggsopplysning> tilleggsopplysninger;
        private List<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjoner;

        private Saksrelasjon saksrelasjon;

        @Data
        @Builder
        public static class Bruker {
            private String brukerType;
        }

        @Data
        @Builder
        public static class Saksrelasjon {
            private String fagsystem;
        }

        @Data
        @Builder
        public static class Tilleggsopplysning {
            private String key;
            private String value;
        }

        @Data
        @Builder
        public static class Kryssreferanse {
            private String referanseId;
        }

        @Data
        @Builder
        public static class JournalpostDokumentInfoRelasjon {
            private String tilknyttetJournalpostSom;
        }


    }

    @Data
    @Builder
    public static class DokumentInfo {
        private String tittel;
        private List<Fildetaljer> fildetaljerListe;
        private List<Skannetinnhold> skannetInnholdListe;
        private List<JournalpostDokumentInfoRelasjon> journalpostRelasjoner;

        @Data
        @Builder
        public static class Fildetaljer {
            private String filtype;
        }

        @Data
        @Builder
        public static class Skannetinnhold {
            private String vedleggNr;
        }

        @Data
        @Builder
        public static class JournalpostDokumentInfoRelasjon {
            private String tilknyttetJournalpostSom;
        }
    }


}
