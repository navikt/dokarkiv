package no.nav.dokarkiv.core.consumer.azure;

import no.nav.security.token.support.core.jwt.JwtToken;
import no.nav.security.token.support.core.jwt.JwtTokenClaims;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class CacheAzureTokenClient {

	static final String DEFAULT_CLAIM_OID = "oid";
	static final String DEFAULT_CLAIM_SUB = "sub";

	private final AzureToken azureToken;

	public CacheAzureTokenClient(AzureToken azureToken) {
		this.azureToken = azureToken;
	}

	public String getAndCacheAzureOnBehalfOfAndClientCredentialToken(String accessToken, String scope, String sub) {
			if (StringUtils.isNotBlank(accessToken) && isOnBehalfOfAzureToken(accessToken)) {
				return azureToken.onBehalfOfAccessToken(accessToken, scope, sub);
			}
		return azureToken.clientCredentialAccessToken(scope);
	}

	private boolean isOnBehalfOfAzureToken(String accessToken) {
		JwtToken jwtToken = new JwtToken(accessToken);
		JwtTokenClaims jwtTokenClaims = jwtToken.getJwtTokenClaims();
		return jwtTokenClaims.getStringClaim(DEFAULT_CLAIM_SUB) != null && jwtTokenClaims.getStringClaim(DEFAULT_CLAIM_OID) != null
				&& !jwtTokenClaims.getStringClaim(DEFAULT_CLAIM_SUB).equals(jwtTokenClaims.getStringClaim(DEFAULT_CLAIM_OID));
	}
}
