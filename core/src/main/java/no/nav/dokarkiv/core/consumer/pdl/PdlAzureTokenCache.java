package no.nav.dokarkiv.core.consumer.pdl;

import com.google.common.hash.Hashing;
import no.nav.dokarkiv.core.consumer.azure.AzureToken;
import no.nav.dokarkiv.core.properties.DokarkivProperties;
import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.security.token.support.core.jwt.JwtToken;
import no.nav.security.token.support.core.jwt.JwtTokenClaims;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static no.nav.dokarkiv.core.cache.CacheConfig.TOKEN_FROM_REQUEST;
import static no.nav.dokarkiv.core.security.SporingHandlerInterceptor.ISSUER_AZUREV2;

@Component
public class PdlAzureTokenCache {

	private static final String DEFAULT_CLAIM_OID = "oid";
	private static final String DEFAULT_CLAIM_SUB = "sub";

	private final CacheManager cacheManager;
	private final AzureToken azureToken;
	private final TokenValidationContextHolder tokenValidationContextHolder;
	private final DokarkivProperties dokarkivProperties;

	public PdlAzureTokenCache(CacheManager cacheManager, AzureToken azureToken,
							  TokenValidationContextHolder tokenValidationContextHolder, DokarkivProperties dokarkivProperties) {
		this.cacheManager = cacheManager;
		this.azureToken = azureToken;
		this.tokenValidationContextHolder = tokenValidationContextHolder;
		this.dokarkivProperties = dokarkivProperties;
	}

	public String azureAccessToken() {
		TokenValidationContext tokenValidationContext = tokenValidationContextHolder.getTokenValidationContext();
		JwtToken jwtToken = tokenValidationContext.getJwtToken(ISSUER_AZUREV2);

		if (!isAccessTokenFromRequestNull(jwtToken)) {
			String cacheKey = Hashing.sha256().hashString(jwtToken.getTokenAsString(), StandardCharsets.UTF_8).toString();
			String accessTokenFromRequest = getAccessTokenFromRequest(cacheKey, jwtToken);
			if (tokenValidationContext.getJwtTokenAsOptional(ISSUER_AZUREV2).isPresent() && isOnBehalfOfToken(jwtToken)) {
				return azureToken.onBehalfOfAccessToken(accessTokenFromRequest, dokarkivProperties.getEndpoints().getPdl().getScope());
			}
		}
		return azureToken.onBehalfOfAccessToken(null, dokarkivProperties.getEndpoints().getPdl().getScope());
	}

	private String getAccessTokenFromRequest(String cacheKey, JwtToken jwtToken) {
		Cache tokenRequestCache = cacheManager.getCache(TOKEN_FROM_REQUEST);
		if (nonNull(tokenRequestCache)) {
			return tokenRequestCache.get(cacheKey, String.class);
		}
		cacheManager.getCache(TOKEN_FROM_REQUEST).put(cacheKey, jwtToken.getTokenAsString());
		return jwtToken.getTokenAsString();
	}

	private boolean isAccessTokenFromRequestNull(JwtToken jwtToken) {
		return isNull(jwtToken) || StringUtils.isBlank(jwtToken.getTokenAsString());
	}

	private boolean isOnBehalfOfToken(JwtToken token) {
		final JwtTokenClaims jwtTokenClaims = token.getJwtTokenClaims();
		return jwtTokenClaims.getStringClaim(DEFAULT_CLAIM_SUB) != null &&
				jwtTokenClaims.getStringClaim(DEFAULT_CLAIM_OID) != null &&
				!jwtTokenClaims.getStringClaim(DEFAULT_CLAIM_SUB).equals(jwtTokenClaims.getStringClaim(DEFAULT_CLAIM_OID));
	}
}
