package no.nav.dokarkiv.core.consumer.azure;

import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static no.nav.dokarkiv.core.NavHeaders.BEARER_TOKEN_PREFIX;
import static no.nav.dokarkiv.core.consumer.azure.AzureToken.isOnBehalfOfAzureToken;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

public record WebClientAzureAuthentication(AzureToken azureToken, String scope) implements ExchangeFilterFunction {

	@Override
	public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
		String tokenValue = accessTokenFromRequest(request);
		String accessToken = isNotBlank(tokenValue) && isOnBehalfOfAzureToken(tokenValue) ? azureToken.onBehalfOfAccessToken(tokenValue, scope) : azureToken.clientCredentialAccessToken(scope);

		return next.exchange(ClientRequest.from(request).headers((headers) ->
						headers.setBearerAuth(accessToken))
				.build());
	}

	private String accessTokenFromRequest(ClientRequest request) {
		return Optional.ofNullable(request.headers().getFirst(AUTHORIZATION))
				.filter(e -> e.startsWith(BEARER_TOKEN_PREFIX))
				.map(e -> e.replaceFirst(BEARER_TOKEN_PREFIX, ""))
				.orElse(null);
	}
}
