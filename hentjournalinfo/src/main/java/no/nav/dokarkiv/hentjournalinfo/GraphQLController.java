package no.nav.dokarkiv.hentjournalinfo;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.execution.AsyncExecutionStrategy;
import graphql.execution.AsyncSerialExecutionStrategy;
import graphql.schema.GraphQLSchema;
import io.leangen.graphql.GraphQLSchemaGenerator;
import io.leangen.graphql.metadata.strategy.query.AnnotatedResolverBuilder;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.hentjournalinfo.exceptionhandler.GraphQLExceptionHandler;
import no.nav.dokarkiv.hentjournalinfo.mock.MockQuery;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@RestController
@Slf4j
public class GraphQLController {

    private final GraphQL graphQL;
    private final GraphQL mockGraphQL;

    @Inject
    public GraphQLController(List<Query> queryList, MockQuery mockQuery, GraphQLExceptionHandler graphQLExceptionHandler) {
        //Schema generated from query classes
        GraphQLSchemaGenerator schemaGenerator = new GraphQLSchemaGenerator()
                .withResolverBuilders(new AnnotatedResolverBuilder());

        for (Query query : queryList) {
            schemaGenerator = schemaGenerator.withOperationsFromSingleton(query, query.getClass().getGenericSuperclass());
        }

        GraphQLSchema schema = schemaGenerator.generate();
        graphQL = GraphQL.newGraphQL(schema)
                .mutationExecutionStrategy(new AsyncSerialExecutionStrategy(graphQLExceptionHandler))
                .queryExecutionStrategy(new AsyncExecutionStrategy(graphQLExceptionHandler))
                .build();

        //Schema generated from mock query class
        GraphQLSchema mockSchema = new GraphQLSchemaGenerator()
                .withResolverBuilders(new AnnotatedResolverBuilder())
                .withOperationsFromSingleton(mockQuery, MockQuery.class)
                .generate();
        mockGraphQL = GraphQL.newGraphQL(mockSchema)
                .mutationExecutionStrategy(new AsyncSerialExecutionStrategy(graphQLExceptionHandler))
                .queryExecutionStrategy(new AsyncExecutionStrategy(graphQLExceptionHandler))
                .build();
    }

    @PostMapping(value = "/rest/graphql", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @RestMetrics(value = "dok_request", extraTags = {"process_code", "gjoark00x"}, percentiles = {0.5, 0.95})
    public Map<String, Object> graphQLRequest(@RequestBody GraphQLRequest request, HttpServletRequest raw) {
        ExecutionResult executionResult = graphQL.execute(ExecutionInput.newExecutionInput()
                .query(request.getQuery())
                .operationName(request.getOperationName())
                .variables(request.getVariables())
                .context(raw)
                .build());
        return executionResult.toSpecification();
    }

    @PostMapping(value = "/mock/graphql", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @RestMetrics(value = "dok_request", extraTags = {"process_code", "gjoark00x_mock"}, percentiles = {0.5, 0.95})
    public Map<String, Object> mockGraphQL(@RequestBody GraphQLRequest request, HttpServletRequest raw) {
        ExecutionResult executionResult = mockGraphQL.execute(ExecutionInput.newExecutionInput()
                .query(request.getQuery())
                .operationName(request.getOperationName())
                .variables(request.getVariables())
                .context(raw)
                .build());
        return executionResult.toSpecification();
    }
}
