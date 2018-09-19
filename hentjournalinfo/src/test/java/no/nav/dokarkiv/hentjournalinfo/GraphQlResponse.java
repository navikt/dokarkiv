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
        dokumentInfo = new ObjectMapper().readValue(data.get("dokumentInfo").toString(), DokumentInfo.class);
        journalpost = new ObjectMapper().readValue(data.get("journalpost").toString(), Journalpost.class);
        dokumentFil = data.get("dokumentFil").toString();
    }

}
