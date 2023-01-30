package no.nav.dokarkiv.core.consumer.azure;

import no.nav.dokarkiv.core.exceptions.AzureTokenException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import static no.nav.dokarkiv.core.util.ConverterUtils.getSubJwtTokenClaim;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class WebClientAzureAuthentication implements ExchangeFilterFunction {

	final private AzureToken azureToken;
	final private String scope;

	public WebClientAzureAuthentication(AzureToken azureToken, String scope) {
		this.azureToken = azureToken;
		this.scope = scope;
	}

	@Override
	public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {

		String tokenValue = getTokenValueFromAccessToken(request.headers().getFirst(HttpHeaders.AUTHORIZATION));
		String sub = isBlank(tokenValue) ? null : getSubJwtTokenClaim(tokenValue);

		return next.exchange(ClientRequest.from(request).headers((headers) ->
				headers.setBearerAuth(azureToken.onBehalfOfAccessToken(tokenValue, scope, sub))).build());
	}

	private String getTokenValueFromAccessToken(String authHeader) {
		try {
			return StringUtils.split(authHeader, " ")[1];
		} catch (Exception e) {
			throw new AzureTokenException(
					String.format("Klarte ikke hente value fra Access Token. Feilemelding=%s", e.getMessage()),
					e.getCause());
		}
	}
}
