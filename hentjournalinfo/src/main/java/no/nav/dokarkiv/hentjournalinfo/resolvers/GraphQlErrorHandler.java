package no.nav.dokarkiv.hentjournalinfo.resolvers;

import graphql.ErrorType;
import graphql.ExceptionWhileDataFetching;
import graphql.GraphQLError;
import graphql.servlet.GraphQLErrorHandler;
import no.nav.dokarkiv.hentjournalinfo.error.GraphQlException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Component
public class GraphQlErrorHandler implements GraphQLErrorHandler {
    @Override
    public boolean errorsPresent(List<GraphQLError> errors) {
        return !errors.isEmpty();
    }

    @Override
    public List<GraphQLError> processErrors(List<GraphQLError> list) {
        return list.stream().map(error -> {
            if (error instanceof ExceptionWhileDataFetching) {
                return new GraphQlException(((ExceptionWhileDataFetching) error).getException()
                        .getMessage(), ErrorType.DataFetchingException);
            }
            return error;
        }).collect(Collectors.toList());
    }
}
