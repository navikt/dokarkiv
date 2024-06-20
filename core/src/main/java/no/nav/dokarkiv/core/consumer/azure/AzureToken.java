package no.nav.dokarkiv.core.consumer.azure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.exceptions.AzureTokenException;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import no.nav.dokarkiv.core.security.azure.AzureConfig;
import no.nav.security.token.support.core.jwt.JwtToken;
import no.nav.security.token.support.core.jwt.JwtTokenClaims;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import static no.nav.dokarkiv.core.cache.CacheConfig.AZURE_CLIENT_CREDENTIAL_GRAPH_TOKEN_CACHE;
import static no.nav.dokarkiv.core.cache.CacheConfig.AZURE_ON_BEHALF_OF_TOKEN_CACHE;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

@Slf4j
@Component
public class AzureToken {

	private static final String ON_BEHALF_OF_GRANT_TYPE = "urn:ietf:params:oauth:grant-type:jwt-bearer";
	private static final String ON_BEHALF_OF = "on_behalf_of";
	private static final String CLIENT_CREDENTIALS = "client_credentials";
	static final String DEFAULT_CLAIM_OID = "oid";
	static final String DEFAULT_CLAIM_SUB = "sub";

	private final AzureConfig azureConfig;
	private final ObjectMapper objectMapper;
	private final WebClient azureClient;

	public AzureToken(AzureConfig azureConfig,
					  ObjectMapper objectMapper,
					  WebClient azureClient) {
		this.azureConfig = azureConfig;
		this.objectMapper = objectMapper;
		this.azureClient = azureClient.mutate()
				.baseUrl(azureConfig.getOpenidConfigTokenEndpoint())
				.defaultHeader(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE)
				.build();
	}

	@Retryable(include = DokarkivFunctionalException.class, backoff = @Backoff(delay = 2000))
	@Cacheable(value = AZURE_ON_BEHALF_OF_TOKEN_CACHE, keyGenerator = "cacheKeyGenerator")
	public String onBehalfOfAccessToken(String token, String scope) {
		return fetchAccessToken(token, scope);
	}

	@Retryable(include = DokarkivFunctionalException.class, backoff = @Backoff(delay = 2000))
	@Cacheable(value = AZURE_CLIENT_CREDENTIAL_GRAPH_TOKEN_CACHE, key = "#scope")
	public String clientCredentialAccessToken(String scope) {
		return fetchAccessToken(null, scope);
	}

	private String fetchAccessToken(String token, String scope) {
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("client_id", azureConfig.getAppClientId());
		formData.add("client_secret", azureConfig.getAppClientSecret());
		formData.add("scope", scope);

		if (token != null) {
			formData.add("requested_token_use", ON_BEHALF_OF);
			formData.add("grant_type", ON_BEHALF_OF_GRANT_TYPE);
			formData.add("assertion", token);
		} else {
			formData.add("grant_type", CLIENT_CREDENTIALS);
		}

		String responseJson = azureClient
				.post()
				.body(BodyInserters.fromFormData(formData))
				.retrieve()
				.bodyToMono(String.class)
				.doOnError(this::handleError)
				.block();

		try {
			return objectMapper.readValue(responseJson, TokenResponse.class).accessToken();
		} catch (JsonProcessingException e) {
			throw new AzureTokenException(String.format("Klarte ikke parse token fra Azure. Feilmelding=%s", e.getMessage()), e);
		}
	}

	private void handleError(Throwable error) {
		if (error instanceof WebClientResponseException response && ((WebClientResponseException) error).getStatusCode().is4xxClientError()) {
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

	public static boolean isOnBehalfOfAzureToken(String accessToken) {
		JwtToken jwtToken = new JwtToken(accessToken);
		JwtTokenClaims jwtTokenClaims = jwtToken.getJwtTokenClaims();
		return jwtTokenClaims.getStringClaim(DEFAULT_CLAIM_SUB) != null && jwtTokenClaims.getStringClaim(DEFAULT_CLAIM_OID) != null
				&& !jwtTokenClaims.getStringClaim(DEFAULT_CLAIM_SUB).equals(jwtTokenClaims.getStringClaim(DEFAULT_CLAIM_OID));
	}
}
