package no.nav.dokarkiv.hentjournalinfo.handlers;

import static graphql.Assert.assertNotNull;
import static java.lang.String.format;

import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.GraphqlErrorHelper;
import graphql.execution.ExecutionPath;
import graphql.language.SourceLocation;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import no.nav.dokarkiv.core.security.abac.AuthorizationException;
import no.nav.dokarkiv.hentjournalinfo.dto.ExceptionType;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Copy of @ExceptionWhileDataFetching
 *
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class CustomExceptionWhileDataFetching implements GraphQLError {
    private final String message;
    private final List<Object> path;
    private final Throwable exception;
    private final List<SourceLocation> locations;
    private final Map<String, Object> extensions;
    private final ExceptionType exceptionType;

    public CustomExceptionWhileDataFetching(ExecutionPath path, Throwable exception, SourceLocation sourceLocation) {
        this.path = assertNotNull(path).toList();
        this.exception = assertNotNull(exception);
        this.locations = Collections.singletonList(sourceLocation);
        this.extensions = mkExtensions(exception);
        this.message = mkMessage(path, exception);
        this.exceptionType = getExceptionTypeFromThrowable(exception);
    }

    public ExceptionType getExceptionType() {
        return this.exceptionType;
    }

    public ExceptionType getExceptionTypeFromThrowable(Throwable exception) {
        if (exception instanceof DokarkivFunctionalException || exception instanceof AuthorizationException) {
            return ExceptionType.FUNCTIONAL;
        }
        return ExceptionType.TECHNICAL;
    }

    private String mkMessage(ExecutionPath path, Throwable exception) {
        return format("Feilet ved henting av data (%s) : %s", path, exception.getMessage());
    }

    /*
     * This allows a DataFetcher to throw a graphql error and have "extension data" be transferred from that
     * exception into the ExceptionWhileDataFetching error and hence have custom "extension attributes"
     * per error message.
     */
    private Map<String, Object> mkExtensions(Throwable exception) {
        Map<String, Object> extensions = null;
        if (exception instanceof GraphQLError) {
            Map<String, Object> map = ((GraphQLError) exception).getExtensions();
            if (map != null) {
                extensions = new LinkedHashMap<>();
                extensions.putAll(map);
            }
        }
        return extensions;
    }

    public Throwable getException() {
        return exception;
    }


    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public List<SourceLocation> getLocations() {
        return locations;
    }

    public List<Object> getPath() {
        return path;
    }

    @Override
    public Map<String, Object> getExtensions() {
        return extensions;
    }

    @Override
    public ErrorType getErrorType() {
        return ErrorType.DataFetchingException;
    }

    @Override
    public String toString() {
        return "ExceptionWhileDataFetching{" +
                "path=" + path +
                "exception=" + exception +
                "locations=" + locations +
                '}';
    }

    @Override
    public Map<String, Object> toSpecification() {
        Map<String, Object> specification = GraphqlErrorHelper.toSpecification(this);
        specification.put("exceptionType", getExceptionType());
        specification.put("exception", getException().getClass().getSimpleName());
        return specification;
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object o) {
        return GraphqlErrorHelper.equals(this, o);
    }

    @Override
    public int hashCode() {
        return GraphqlErrorHelper.hashCode(this);
    }
}
