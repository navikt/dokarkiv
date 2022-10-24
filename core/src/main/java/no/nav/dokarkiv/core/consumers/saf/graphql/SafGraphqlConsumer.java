package no.nav.dokarkiv.core.consumers.saf.graphql;

import no.nav.dokarkiv.core.consumer.azure.AzureToken;
import no.nav.dokarkiv.core.consumer.azure.WebClientAzureAuthentication;
import no.nav.dokarkiv.core.consumers.saf.exceptions.saf.SafJournalpostQueryTechnicalException;
import no.nav.dokarkiv.core.consumers.saf.exceptions.saf.SafJournalpostUnauthorizedException;
import no.nav.dokarkiv.core.exceptions.ValidationFunctionalException;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.util.NavHeadersFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import static no.nav.dokarkiv.core.storage.RetryConstants.DELAY_SHORT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
public class SafGraphqlConsumer {

	private final WebClient safGraphQLClient;

	@Autowired
	public SafGraphqlConsumer(SafGraphQLConfig safGraphQLConfig,
							  AzureToken azureToken,
							  WebClient safGraphQLClient) {
		this.safGraphQLClient = safGraphQLClient
				.mutate()
				.filter(new NavHeadersFilter())
				.filter(new WebClientAzureAuthentication(azureToken, safGraphQLConfig.getScope()))
				.build();
	}

	@RestMetrics(value = "dok_request", extraTags = {"process_code", "safJournalpostQuery"}, percentiles = {0.5, 0.95})
	@Retryable(include = SafJournalpostQueryTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT))
	public ResponseEntity<String> performQuery(GraphQLRequest graphQLRequest, String safAuthorizationHeader) {

		return safGraphQLClient
				.post()
				.header(AUTHORIZATION, safAuthorizationHeader)
				.bodyValue(graphQLRequest)
				.retrieve()
				.toEntity(String.class)
				.doOnError(this::handleError)
				.block();
	}

	private void handleError(Throwable error) {
		if(error instanceof WebClientResponseException response && ((WebClientResponseException) error).getStatusCode().is4xxClientError()) {
			throw new SafJournalpostUnauthorizedException(
					String.format("Tjenesten SAF (graphQL) feilet funksjonelt med status: %s, feilmelding: %s",
							response.getRawStatusCode(),
							response.getMessage()),
					error);
		} else {
			throw new SafJournalpostQueryTechnicalException(
					String.format("Tjenesten SAF (graphQL) feilet teknisk med feilmelding: %s", error.getMessage()),
					error);
		}
	}
}
