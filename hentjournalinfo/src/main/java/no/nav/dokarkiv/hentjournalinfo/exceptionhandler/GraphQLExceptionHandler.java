package no.nav.dokarkiv.hentjournalinfo.exceptionhandler;

import static no.nav.dokarkiv.core.metrics.MetricUtils.incrementExceptionCounter;

import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.ExecutionPath;
import graphql.language.SourceLocation;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Slf4j
@Component
public class GraphQLExceptionHandler implements DataFetcherExceptionHandler {

    @Inject
    private MeterRegistry meterRegistry;

    @Override
    public void accept(DataFetcherExceptionHandlerParameters handlerParameters) {
        Throwable exception = handlerParameters.getException();
        SourceLocation sourceLocation = handlerParameters.getField().getSourceLocation();
        ExecutionPath path = handlerParameters.getPath();

        CustomExceptionWhileDataFetching error = new CustomExceptionWhileDataFetching(path, exception, sourceLocation);
        handlerParameters.getExecutionContext().addError(error);
        log.warn(error.getMessage(), exception);

        incrementExceptionCounter("dok_graphql_request_exception", error.getException(), meterRegistry, "process_code", "gjoark00x", "path", getPathAsString(error
                .getPath()));
    }

    private String getPathAsString(List<Object> pathList) {
        return StringUtils.chop(pathList.stream().map(p -> p + "/").collect(Collectors.joining()));
    }
}
