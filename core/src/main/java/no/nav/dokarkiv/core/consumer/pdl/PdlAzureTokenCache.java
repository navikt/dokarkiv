package no.nav.dokarkiv.core.consumer.pdl;

import com.google.common.hash.Hashing;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.consumer.azure.AzureToken;
import no.nav.dokarkiv.core.properties.DokarkivProperties;
import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.security.token.support.core.jwt.JwtToken;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static no.nav.dokarkiv.core.cache.CacheConfig.TOKEN_FROM_REQUEST;
import static no.nav.dokarkiv.core.security.SporingHandlerInterceptor.ISSUER_AZUREV2;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@Component
public class PdlAzureTokenCache {

	static final String OPTIONAL_CLAIM_SET_IDTYP = "idtyp";
	static final String OPTIONAL_CLAIM_SET_IDTYP_VALUE = "app";

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
			if (!jwtToken.containsClaim(OPTIONAL_CLAIM_SET_IDTYP, OPTIONAL_CLAIM_SET_IDTYP_VALUE)) {
				log.info("Mottatt kall til å hente On-Behalf-Of Access Token fra Azure");
				return azureToken.onBehalfOfAccessToken(accessTokenFromRequest, dokarkivProperties.getEndpoints().getPdl().getScope());
			}
		}
		log.info("Mottatt kall til å hente client credential Access Token fra Azure");
		return azureToken.clientCredentialAccessToken(dokarkivProperties.getEndpoints().getPdl().getScope());
	}

	private String getAccessTokenFromRequest(String cacheKey, JwtToken jwtToken) {
		Cache tokenRequestCache = cacheManager.getCache(TOKEN_FROM_REQUEST);
		if (nonNull(tokenRequestCache)) {
			log.info("Hentet cached token");
			return tokenRequestCache.get(cacheKey, String.class);
		}
		cacheManager.getCache(TOKEN_FROM_REQUEST).put(cacheKey, jwtToken.getTokenAsString());
		return jwtToken.getTokenAsString();
	}

	private boolean isAccessTokenFromRequestNull(JwtToken jwtToken) {
		return isNull(jwtToken) || isBlank(jwtToken.getTokenAsString());
	}
}
