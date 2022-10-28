package no.nav.dokarkiv.core.consumer.azure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.exceptions.AzureTokenException;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import no.nav.dokarkiv.core.security.azure.AzureConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

import static no.nav.dokarkiv.core.cache.CacheConfig.AZURE_CLIENT_CREDENTIAL_GRAPH_TOKEN_CACHE;

@Slf4j
@Component
public class AzureToken {

    private static final String ON_BEHALF_OF_GRANT_TYPE = "urn:ietf:params:oauth:grant-type:jwt-bearer";
    private static final String ON_BEHALF_OF = "on_behalf_of";
    private static final String AZURE_TOKEN_INSTANCE = "azuretoken";
    private static final String CLIENT_CREDENTIALS = "client_credentials";

    private final AzureConfig azureConfig;
    private final ObjectMapper objectMapper;
    private final WebClient azureClient;

    public AzureToken(AzureConfig azureConfig,
					  ObjectMapper objectMapper,
					  WebClient azureClient) {
        this.azureConfig = azureConfig;
        this.objectMapper = objectMapper;
        this.azureClient = azureClient;
    }

    @Retryable(include = DokarkivFunctionalException.class, backoff = @Backoff(delay = 2000))
    public String onBehalfOfAccessToken(String token, String scope) {
        return fetchAccessToken(token, scope);
    }

    private String fetchAccessToken(String token, String scope) {

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", azureConfig.getAppClientId());
        formData.add("client_secret", azureConfig.getAppClientSecret());
        formData.add("scope", scope);

        formData.add("requested_token_use", ON_BEHALF_OF);
        formData.add("grant_type", ON_BEHALF_OF_GRANT_TYPE);
        formData.add("assertion", token);

        String responseJson = azureConsumer(formData);
        try {
            Map<String, Object> tokenData = objectMapper.readValue(responseJson, Map.class);
            return (String) tokenData.get("access_token");
        } catch (JsonProcessingException | ClassCastException e) {
            throw new AzureTokenException(String.format("Klarte ikke parse token fra Azure. Feilmelding=%s", e.getMessage()), e);
        }
    }

    @Retry(name = AZURE_TOKEN_INSTANCE)
    @CircuitBreaker(name = AZURE_TOKEN_INSTANCE)
    @Cacheable(AZURE_CLIENT_CREDENTIAL_GRAPH_TOKEN_CACHE)
    public TokenResponse getClientCredentialToken(String scope) {
        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("client_id", azureConfig.getAppClientId());
            formData.add("client_secret", azureConfig.getAppClientSecret());
            formData.add("scope", scope);
            formData.add("grant_type", CLIENT_CREDENTIALS);
            String responseJson = azureConsumer(formData);
            try {
                return objectMapper.readValue(responseJson, TokenResponse.class);
            } catch (JsonProcessingException | ClassCastException e) {
                throw new AzureTokenException(String.format("Klarte ikke parse token fra Azure. Feilmelding=%s", e.getMessage()), e);
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new AzureTokenException(String.format("Klarte ikke hente token fra Azure. Feilet med httpstatus=%s. Feilmelding=%s", e.getStatusCode(), e.getMessage()), e);
        }
    }

    private String azureConsumer(MultiValueMap<String, String> formData) {
        return azureClient
                .post()
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(this::handleError)
                .block();

    }

    private void handleError(Throwable error) {
        if(error instanceof WebClientResponseException response && ((WebClientResponseException) error).getStatusCode().is4xxClientError()) {
            throw new AzureTokenException(
                    String.format("Klarte ikke hente token fra Azure. Feilet med statuskode=%s Feilmelding=%s",
                            response.getRawStatusCode(),
                            response.getMessage()),
                    error);
        } else {
            throw new AzureTokenException(
                    String.format("Kall mot Azure feilet med feilmelding=%s", error.getMessage()),
                    error);
        }
    }
}
