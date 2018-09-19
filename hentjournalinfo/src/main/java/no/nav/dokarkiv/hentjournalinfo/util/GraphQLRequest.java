package no.nav.dokarkiv.hentjournalinfo.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Builder
@Getter
public class GraphQLRequest {
    private final String query;
    private final String operationName;
    private final Map<String, Object> variables;

    @JsonCreator
    public GraphQLRequest(@JsonProperty("query") String query,
                          @JsonProperty("operationName") String operationName,
                          @JsonProperty("variables") Map<String, Object> variables) {
        this.query = query;
        this.operationName = operationName;
        this.variables = variables;
    }
}
