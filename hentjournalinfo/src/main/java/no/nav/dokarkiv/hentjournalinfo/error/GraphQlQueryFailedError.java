package no.nav.dokarkiv.hentjournalinfo.error;

import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.language.SourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class GraphQlQueryFailedError implements GraphQLError {
    private final String message;
    private final ErrorType errorType;

    public GraphQlQueryFailedError(String message, ErrorType errorType) {
        this.message = message;
        this.errorType = errorType;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public List<SourceLocation> getLocations() {
        return new ArrayList<>();
    }

    @Override
    public ErrorType getErrorType() {
        return errorType;
    }
}
