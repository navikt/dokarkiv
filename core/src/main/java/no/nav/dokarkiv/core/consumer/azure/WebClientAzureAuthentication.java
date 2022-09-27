package no.nav.dokarkiv.core.consumer.azure;

import org.apache.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

public class WebClientAzureAuthentication implements ExchangeFilterFunction {
    final private AzureToken azureToken;
    final private String scope;

    public WebClientAzureAuthentication(AzureToken azureToken, String scope) {
        this.azureToken = azureToken;
        this.scope = scope;
    }

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {

        var token = request.headers().getFirst(HttpHeaders.AUTHORIZATION);

        return next.exchange(ClientRequest.from(request).headers((headers) ->
                headers.setBearerAuth(azureToken.accessToken(token, scope))).build());
    }
}
