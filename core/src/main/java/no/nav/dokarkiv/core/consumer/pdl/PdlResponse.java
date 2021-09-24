package no.nav.dokarkiv.core.consumer.pdl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
public class PdlResponse {
    private PdlHentIdenter data;
    private List<PdlError> errors;

    @Data
    public static class PdlHentIdenter {
        private PdlIdenter hentIdenter;
        private PdlPerson hentPerson;
    }

    @Data
    public static class PdlIdenter {
        private List<PdlIdent> identer;
    }

    @Data
    public static class PdlIdent {
        @ToString.Exclude
        private String ident;
        private boolean historisk;
        private PdlGruppe gruppe;
    }

    @Data
    public static class PdlPerson {
        private List<PdlNavn> navn;
    }

    @Data
    public static class PdlNavn {
        @ToString.Exclude
        private String fornavn;
        private String mellomnavn;
        private String etternavn;
    }

    @Data
    @JsonIgnoreProperties({"locations", "path"})
    public static class PdlError {
        private String message;
        private PdlErrorExtension extensions;
    }

    @Data
    public static class PdlErrorExtension {
        private String code;
        private String classification;
        private PdlErrorDetails details;
    }

    @Data
    public static class PdlErrorDetails {
        private String type;
        private String cause;
        private String policy;
    }

    public enum PdlGruppe {
        FOLKEREGISTERIDENT, AKTORID, NPID;
    }

}
