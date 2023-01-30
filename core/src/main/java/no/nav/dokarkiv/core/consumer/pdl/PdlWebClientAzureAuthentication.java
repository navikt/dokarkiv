package no.nav.dokarkiv.core.consumer.pdl;

import no.nav.dokarkiv.core.consumer.azure.CacheAzureTokenClient;
import no.nav.dokarkiv.core.properties.DokarkivProperties;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static no.nav.dokarkiv.core.NavHeaders.BEARER_TOKEN_PREFIX;
import static no.nav.dokarkiv.core.util.ConverterUtils.getSubJwtTokenClaim;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

public record PdlWebClientAzureAuthentication(CacheAzureTokenClient cacheAzureTokenClient,
											  DokarkivProperties dokarkivProperties) implements ExchangeFilterFunction {

	@Override
	public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
		String accessToken = accessTokenFromRequest(request);
		return next.exchange(ClientRequest.from(request)
				.headers(httpHeaders -> {
					httpHeaders.setBearerAuth(cacheAzureTokenClient.getAndCacheAzureOnBehalfOfAndClientCredentialToken(accessToken,
							dokarkivProperties.getEndpoints().getPdl().getScope(),
							getSubJwtTokenClaim(accessToken)));
				})
				.build());
	}

	private String accessTokenFromRequest(ClientRequest request) {
		return Optional.ofNullable(request.headers().getFirst(AUTHORIZATION))
				.filter(e -> e.startsWith(BEARER_TOKEN_PREFIX))
				.map(e -> e.replaceFirst(BEARER_TOKEN_PREFIX, ""))
				.orElse(null);
	}
}
