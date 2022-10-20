package no.nav.dokarkiv.core.consumer.azure;

public interface TokenConsumer {
	TokenResponse getClientCredentialToken(String token);
}
