package no.nav.dokarkiv.core.consumer.azure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTParser;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.exceptions.AzureTokenException;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import no.nav.dokarkiv.core.security.azure.AzureConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

import static no.nav.dokarkiv.core.cache.CacheConfig.AZURE_TOKEN_CACHE;

@Slf4j
@Component
public class AzureToken {

    private static final String CLIENT_CREDENTIALS_GRANT_TYPE = "client_credentials";
    private static final String ON_BEHALF_OF_GRANT_TYPE = "urn:ietf:params:oauth:grant-type:jwt-bearer";
    private static final String ON_BEHALF_OF = "on_behalf_of";
    private static final String OID_CLAIM_NAME = "oid";

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
    @Cacheable(AZURE_TOKEN_CACHE)
    public String accessToken(String token, String scope) {
		return (isOnBehalfOfToken(token) || token == null) ? fetchAccessToken(token, scope) : token;
    }

    private String fetchAccessToken(String token, String scope) {

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", azureConfig.getAppClientId());
        formData.add("client_secret", azureConfig.getAppClientSecret());
        formData.add("scope", scope);

        if(isOnBehalfOfToken(token)) {
            formData.add("request_token_use", ON_BEHALF_OF);
            formData.add("grant_type", ON_BEHALF_OF_GRANT_TYPE);
            formData.add("assertions", getTokenValueFromAccessToken(token));
        } else {
            formData.add("grant_type", CLIENT_CREDENTIALS_GRANT_TYPE);
        }

        String responseJson = azureClient.post()
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(this::handleError)
                .block();

        try {
            Map<String, Object> tokenData = objectMapper.readValue(responseJson, Map.class);
            return (String) tokenData.get("access_token");
        } catch (JsonProcessingException | ClassCastException e) {
            throw new AzureTokenException(String.format("Klarte ikke parse token fra Azure. Feilmelding=%s", e.getMessage()), e);
        }
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

    private boolean isOnBehalfOfToken(String token) {

        if(token == null) {
            return false;
        }

        try {
            var jwtToken = JWTParser.parse(token);
            var oid = jwtToken.getJWTClaimsSet().getClaim(OID_CLAIM_NAME).toString();
            var sub = jwtToken.getJWTClaimsSet().getSubject();
            return oid != null && !StringUtils.equals(oid, sub);
        } catch (Exception e) {
            throw new AzureTokenException(
                    String.format("En feil oppsto ved behandling av Access Token. Feilemelding=%s", e.getMessage()),
                    e.getCause());
        }
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
