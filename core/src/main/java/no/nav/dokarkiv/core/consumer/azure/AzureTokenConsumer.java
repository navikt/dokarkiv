package no.nav.dokarkiv.core.consumer.azure;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import no.nav.dokarkiv.core.exceptions.AzureTokenException;
import no.nav.dokarkiv.core.security.azure.AzureConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

import static no.nav.dokarkiv.core.cache.CacheConfig.AZURE_CLIENT_CREDENTIAL_GRAPH_TOKEN_CACHE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;


@Component
@Profile({"nais", "local"})
public class AzureTokenConsumer implements TokenConsumer {
	private static final String AZURE_TOKEN_INSTANCE = "azuretoken";
	private final RestTemplate restTemplate;
	private final AzureConfig azureConfig;

	public AzureTokenConsumer(AzureConfig azureConfig,
							  RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
		this.azureConfig = azureConfig;
	}

	@Retry(name = AZURE_TOKEN_INSTANCE)
	@CircuitBreaker(name = AZURE_TOKEN_INSTANCE)
	@Cacheable(AZURE_CLIENT_CREDENTIAL_GRAPH_TOKEN_CACHE)
	public TokenResponse getClientCredentialToken(String scope) {
		try {
			HttpHeaders headers = createHeaders();
			String form = "grant_type=client_credentials&scope=" + scope + "&client_id=" +
					azureConfig.getAppClientId() + "&client_secret=" + azureConfig.getAppClientSecret();
			HttpEntity<String> requestEntity = new HttpEntity<>(form, headers);

			return restTemplate.exchange(azureConfig.getOpenidConfigTokenEndpoint(), POST, requestEntity, TokenResponse.class)
					.getBody();
		} catch (HttpClientErrorException | HttpServerErrorException e) {
			throw new AzureTokenException(String.format("Klarte ikke hente token fra Azure. Feilet med httpstatus=%s. Feilmelding=%s", e.getStatusCode(), e.getMessage()), e);
		}
	}

	private HttpHeaders createHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(APPLICATION_FORM_URLENCODED);
		headers.setAccept(Collections.singletonList(APPLICATION_JSON));
		return headers;
	}
}