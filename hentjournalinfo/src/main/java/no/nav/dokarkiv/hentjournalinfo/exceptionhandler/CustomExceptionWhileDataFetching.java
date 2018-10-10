package no.nav.dokarkiv.hentjournalinfo.exceptionhandler;

import static graphql.Assert.assertNotNull;
import static java.lang.String.format;
import static no.nav.dokarkiv.core.metrics.MetricUtils.isFunctionalException;

import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.GraphqlErrorHelper;
import graphql.execution.ExecutionPath;
import graphql.language.SourceLocation;
import lombok.ToString;
import no.nav.dokarkiv.hentjournalinfo.dto.kode.ExceptionType;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Copy of @ExceptionWhileDataFetching
 *
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@ToString
public class CustomExceptionWhileDataFetching implements GraphQLError {
    private final String message;
    private final List<Object> path;
    private final Throwable exception;
    private final List<SourceLocation> locations;
    private final ExceptionType exceptionType;

    public CustomExceptionWhileDataFetching(ExecutionPath path, Throwable exception, SourceLocation sourceLocation) {
        this.path = assertNotNull(path).toList();
        this.exception = assertNotNull(exception);
        this.locations = Collections.singletonList(sourceLocation);
        this.message = mkMessage(path, exception);
        this.exceptionType = getExceptionTypeFromThrowable(exception);
    }

    public ExceptionType getExceptionType() {
        return this.exceptionType;
    }

    public ExceptionType getExceptionTypeFromThrowable(Throwable exception) {
        if (isFunctionalException(exception)) {
            return ExceptionType.FUNCTIONAL;
        }
        return ExceptionType.TECHNICAL;
    }

    private String mkMessage(ExecutionPath path, Throwable exception) {
        return format("Feilet ved henting av data (%s) : %s", path, exception.getMessage());
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
    public ErrorType getErrorType() {
        return ErrorType.DataFetchingException;
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
