package no.nav.dokarkiv.core.consumers.saf.graphql;

import no.nav.dokarkiv.core.consumer.azure.AzureToken;
import no.nav.dokarkiv.core.consumer.azure.WebClientAzureAuthentication;
import no.nav.dokarkiv.core.consumers.saf.exceptions.saf.SafJournalpostQueryTechnicalException;
import no.nav.dokarkiv.core.consumers.saf.exceptions.saf.SafJournalpostUnauthorizedException;
import no.nav.dokarkiv.core.properties.DokarkivProperties;
import no.nav.dokarkiv.core.util.NavHeadersFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import static no.nav.dokarkiv.core.storage.RetryConstants.DELAY_SHORT;

@Component
public class SafGraphqlConsumer {

	private final WebClient webClient;

	@Autowired
	public SafGraphqlConsumer(AzureToken azureToken, DokarkivProperties dokarkivProperties,
							  WebClient webClient) {
		this.webClient = webClient
				.mutate()
				.baseUrl(dokarkivProperties.getEndpoints().getSaf().getUrl())
				.filter(new NavHeadersFilter())
				.filter(new WebClientAzureAuthentication(azureToken, dokarkivProperties.getEndpoints().getSaf().getScope()))
				.build();
	}

		@Retryable(include = SafJournalpostQueryTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT))
	public ResponseEntity<String> performQuery(GraphQLRequest graphQLRequest) {

		return webClient
				.post()
				.bodyValue(graphQLRequest)
				.retrieve()
				.toEntity(String.class)
				.doOnError(this::handleError)
				.block();
	}

	private void handleError(Throwable error) {
		if (error instanceof WebClientResponseException response && ((WebClientResponseException) error).getStatusCode().is4xxClientError()) {
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
