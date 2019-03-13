package no.nav.dokarkiv.hentjournalinfo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import no.nav.dokarkiv.hentjournalinfo.dto.DokumentInfo;
import no.nav.dokarkiv.hentjournalinfo.dto.Journalpost;
import no.nav.dokarkiv.hentjournalinfo.dto.kode.ExceptionType;

import java.util.List;

/**
 * Example response object for dokarkiv graphql response
 * You should define the variables you need for the request
 *
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GraphQlResponse {

    private DataWrapper dataWrapper;
    private List<Error> errors;

    @JsonCreator
    public GraphQlResponse(@JsonProperty("data") DataWrapper dataWrapper, @JsonProperty("errors") List<Error> errors) {
        this.dataWrapper = dataWrapper;
        this.errors = errors;
    }

    @Data
    public static class DataWrapper {
        private DokumentInfo dokumentInfo;
        private Journalpost journalpost;
        private String dokumentFil;
    }

    @Data
    public static class Error {
        private String message;
        private List<String> path;
        private ExceptionType exceptionType;
        private String exception;
        private List<Locations> locations;

        @Data
        public static class Locations {
            private Long line;
            private Long column;
        }
    }
}
