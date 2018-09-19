package no.nav.dokarkiv.hentjournalinfo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import no.nav.dokarkiv.hentjournalinfo.dto.DokumentInfo;
import no.nav.dokarkiv.hentjournalinfo.dto.Journalpost;

import java.io.IOException;

/**
 * Example response object for dokarkiv graphql response
 * You should define the variables you need for the request
 *
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Data
@Builder
@AllArgsConstructor
public class GraphQlResponse {

    private DokumentInfo dokumentInfo;
    private Journalpost journalpost;
    private String dokumentFil;

    private JsonNode data;

    @JsonCreator
    public GraphQlResponse(@JsonProperty("data") JsonNode data) throws IOException {
        dokumentInfo = mapToObject(data.get("dokumentInfo"), DokumentInfo.class);
        journalpost = mapToObject(data.get("journalpost"), Journalpost.class);
        dokumentFil = data.get("dokumentFil") == null ? null : data.get("dokumentFil").toString();
    }

    private <T> T mapToObject(JsonNode data, Class<T> tClass) throws IOException {
        if (data == null) {
            return null;
        }
        return new ObjectMapper().readValue(data.toString(), tClass);
    }
}
