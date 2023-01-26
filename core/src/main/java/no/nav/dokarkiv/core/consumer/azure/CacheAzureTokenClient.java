package no.nav.dokarkiv.core.consumer.azure;

import com.google.common.hash.Hashing;
import no.nav.dokarkiv.core.cache.CacheToken;
import no.nav.security.token.support.core.jwt.JwtToken;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

import static java.util.Optional.ofNullable;
import static no.nav.dokarkiv.core.security.SporingHandlerInterceptor.ISSUER_AZUREV2;
import static org.springframework.util.StringUtils.hasText;

@Component
public class CacheAzureTokenClient {

	static final String OPTIONAL_CLAIM_SET_IDTYP = "idtyp";
	static final String OPTIONAL_CLAIM_SET_IDTYP_VALUE = "app";

	private final CacheToken tokenCache;
	private final AzureToken azureToken;

	public CacheAzureTokenClient(CacheToken tokenCache, AzureToken azureToken) {
		this.tokenCache = tokenCache;
		this.azureToken = azureToken;
	}

	public String getAndCacheAzureOnBehalfOfAndClientCredentialToken(@NonNull String accessToken, String scope) {

		if (hasText(accessToken)) {
			JwtToken jwtToken = new JwtToken(accessToken);
			String cacheKey = Hashing.sha256().hashString(scope + "-" + jwtToken.getSubject(), StandardCharsets.UTF_8).toString();

			if (ISSUER_AZUREV2.equals(jwtToken.getIssuer()) && !isAzureTokenContainsClaimIdtyp(jwtToken)) {
				return ofNullable(tokenCache)
						.map(cache -> cache.getTokenFromCacheOrProvider(cacheKey, () -> azureToken.onBehalfOfAccessToken(accessToken, scope)))
						.orElseGet(() -> azureToken.onBehalfOfAccessToken(accessToken, scope));
			}
		}
		return ofNullable(tokenCache)
				.map(cache -> cache.getTokenFromCacheOrProvider(scope, () -> azureToken.clientCredentialAccessToken(scope)))
				.orElseGet(() -> azureToken.clientCredentialAccessToken(scope));
	}

	//https://learn.microsoft.com/en-us/azure/active-directory/develop/access-tokens#user-and-application-tokens
	private boolean isAzureTokenContainsClaimIdtyp(JwtToken jwtToken) {
		return jwtToken.containsClaim(OPTIONAL_CLAIM_SET_IDTYP, OPTIONAL_CLAIM_SET_IDTYP_VALUE) ||
				jwtToken.getJwtTokenClaims().getAllClaims().containsKey(OPTIONAL_CLAIM_SET_IDTYP);
	}
}
