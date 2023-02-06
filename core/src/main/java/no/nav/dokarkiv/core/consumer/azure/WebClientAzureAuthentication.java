package no.nav.dokarkiv.core.consumer.azure;

import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static no.nav.dokarkiv.core.NavHeaders.BEARER_TOKEN_PREFIX;
import static no.nav.dokarkiv.core.util.ConverterUtils.getSubJwtTokenClaim;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

public record WebClientAzureAuthentication(AzureToken azureToken, String scope) implements ExchangeFilterFunction {

	@Override
	public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {

		String tokenValue = accessTokenFromRequest(request);
		String sub = isBlank(tokenValue) ? null : getSubJwtTokenClaim(tokenValue);

		return next.exchange(ClientRequest.from(request).headers((headers) ->
				headers.setBearerAuth(azureToken.getAndCacheAzureOnBehalfOfAndClientCredentialToken(tokenValue, scope, sub))).build());
	}

	private String accessTokenFromRequest(ClientRequest request) {
		return Optional.ofNullable(request.headers().getFirst(AUTHORIZATION))
				.filter(e -> e.startsWith(BEARER_TOKEN_PREFIX))
				.map(e -> e.replaceFirst(BEARER_TOKEN_PREFIX, ""))
				.orElse(null);
	}
}
