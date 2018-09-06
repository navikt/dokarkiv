package no.nav.dokarkiv.hentdokument.graphql.error;

import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.language.SourceLocation;

import java.util.List;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class GraphQlException implements GraphQLError {
    private String message;
    private ErrorType errorType;

    public GraphQlException(String message, ErrorType errorType) {
        this.message = message;
        this.errorType = errorType;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public List<SourceLocation> getLocations() {
        return null;
    }

    @Override
    public ErrorType getErrorType() {
        return errorType;
    }
}
