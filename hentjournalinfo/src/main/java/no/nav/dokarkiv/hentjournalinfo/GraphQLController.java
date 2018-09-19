package no.nav.dokarkiv.hentjournalinfo;

import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_JOURNALPOST;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.READ_ACTION;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.execution.AsyncExecutionStrategy;
import graphql.execution.AsyncSerialExecutionStrategy;
import graphql.schema.GraphQLSchema;
import io.leangen.graphql.GraphQLSchemaGenerator;
import io.leangen.graphql.metadata.strategy.query.AnnotatedResolverBuilder;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.hentjournalinfo.handlers.GraphQLExceptionHandler;
import no.nav.dokarkiv.hentjournalinfo.mock.MockQuery;
import no.nav.dokarkiv.hentjournalinfo.query.Query;
import no.nav.dokarkiv.hentjournalinfo.util.GraphQLFieldMetrics;
import no.nav.dokarkiv.hentjournalinfo.util.GraphQLRequest;
import no.nav.freg.abac.core.annotation.Abac;
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
    public GraphQLController(List<Query> queryList, MockQuery mockQuery, GraphQLFieldMetrics graphQLFieldMetrics) {
        //Schema generated from query classes
        GraphQLSchemaGenerator schemaGenerator = new GraphQLSchemaGenerator()
                .withResolverBuilders(new AnnotatedResolverBuilder());

        for (Query query : queryList) {
            schemaGenerator = schemaGenerator.withOperationsFromSingleton(query, query.getClass().getGenericSuperclass());
        }

        GraphQLSchema schema = schemaGenerator.generate();
        graphQL = GraphQL.newGraphQL(schema)
                .mutationExecutionStrategy(new AsyncSerialExecutionStrategy(new GraphQLExceptionHandler()))
                .queryExecutionStrategy(new AsyncExecutionStrategy(new GraphQLExceptionHandler()))
                .instrumentation(graphQLFieldMetrics)
                .build();

        //Schema generated from mock query class
        GraphQLSchema mockSchema = new GraphQLSchemaGenerator()
                .withResolverBuilders(new AnnotatedResolverBuilder())
                .withOperationsFromSingleton(mockQuery, MockQuery.class)
                .generate();
        mockGraphQL = GraphQL.newGraphQL(mockSchema)
                .mutationExecutionStrategy(new AsyncSerialExecutionStrategy(new GraphQLExceptionHandler()))
                .queryExecutionStrategy(new AsyncExecutionStrategy(new GraphQLExceptionHandler()))
                .instrumentation(graphQLFieldMetrics)
                .build();
    }

    @PostMapping(value = "/rest/graphql", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_JOURNALPOST)},
            actions = @Abac.Attr(key = ACTION_ID, value = READ_ACTION))
    @ResponseBody
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
