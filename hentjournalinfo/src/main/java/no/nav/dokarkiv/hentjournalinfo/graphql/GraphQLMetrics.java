package no.nav.dokarkiv.hentjournalinfo.graphql;

import static graphql.execution.instrumentation.SimpleInstrumentationContext.whenCompleted;

import graphql.analysis.QueryTraversal;
import graphql.analysis.QueryVisitorEnvironment;
import graphql.execution.instrumentation.InstrumentationContext;
import graphql.execution.instrumentation.SimpleInstrumentation;
import graphql.execution.instrumentation.parameters.InstrumentationValidationParameters;
import graphql.validation.ValidationError;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Component
public class GraphQLMetrics extends SimpleInstrumentation {

    @Inject
    private MeterRegistry registry;

    @Override
    public InstrumentationContext<List<ValidationError>> beginValidation(InstrumentationValidationParameters parameters) {
        return whenCompleted((errors, throwable) -> {
            if ((errors != null && errors.size() > 0) || throwable != null) {
                return;
            }
            QueryTraversal queryTraversal = newQueryTraversal(parameters);
            int depth = queryTraversal.reducePreOrder((env, acc) -> Math.max(getPathLength(env.getParentEnvironment()), acc), 0);
            Counter.builder("dok_graphql")
                    .tag("depth", String.valueOf(depth))
                            .register(registry)
                            .increment();
        });

    }

    QueryTraversal newQueryTraversal(InstrumentationValidationParameters parameters) {
        return new QueryTraversal(
                parameters.getSchema(),
                parameters.getDocument(),
                parameters.getOperation(),
                parameters.getVariables()
        );
    }

    private int getPathLength(QueryVisitorEnvironment path) {
        int length = 1;
        while (path != null) {
            path = path.getParentEnvironment();
            length++;
        }
        return length;
    }


}
