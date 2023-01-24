package no.nav.dokarkiv.core.consumer.pdl;

import org.slf4j.MDC;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CALL_ID;

public record PdlWebClientAzureAuthentication(PdlTokenCache pdlTokenCache) implements ExchangeFilterFunction {
	@Override
	public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
			return next.exchange(ClientRequest.from(request)
					.headers(httpHeaders -> {
						httpHeaders.setBearerAuth(pdlTokenCache.azureAccessToken());
						httpHeaders.set(MDC_CALL_ID, MDC.get(MDC_CALL_ID));
					})
					.build());
		}
}
