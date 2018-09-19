package no.nav.dokarkiv.hentjournalinfo.util;

import static java.util.Objects.isNull;

import graphql.ExecutionResult;
import graphql.execution.instrumentation.InstrumentationContext;
import graphql.execution.instrumentation.SimpleInstrumentation;
import graphql.execution.instrumentation.parameters.InstrumentationExecutionStrategyParameters;
import graphql.language.Field;
import graphql.language.Selection;
import graphql.language.SelectionSet;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Component
public class GraphQLFieldMetrics extends SimpleInstrumentation {

    @Inject
    private MeterRegistry registry;

    @Override
    public InstrumentationContext<ExecutionResult> beginExecutionStrategy(InstrumentationExecutionStrategyParameters parameters) {

        parameters.getExecutionStrategyParameters().getFields().forEach((queryName, fields) -> {
            SelectionSet selectionSet = fields.get(0).getSelectionSet();
            if (!isNull(selectionSet) && !parameters.getExecutionStrategyParameters().getPath().toString().startsWith("/__")) {
                selectionSet.getSelections().forEach(selection -> {
                    Counter.builder("dok_graphql_request_fields")
                            .tag("query", queryName)
                            .tag("path", parameters.getExecutionStrategyParameters().getPath().toString())
                            .tag("field", getFieldName(selection))
                            .register(registry)
                            .increment();

                });
            }
        });
        return super.beginExecutionStrategy(parameters);
    }

    private String getFieldName(Selection selection) {
        if (selection instanceof Field) {
            return ((Field) selection).getName();
        }
        return "unknown";
    }
}
