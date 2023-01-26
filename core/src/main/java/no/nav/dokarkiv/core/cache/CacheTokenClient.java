package no.nav.dokarkiv.core.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import no.nav.security.token.support.core.jwt.JwtToken;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static java.time.Duration.ofSeconds;
import static java.util.Objects.isNull;

@Component
public class CacheTokenClient implements CacheToken {

	private final Cache<String, JwtToken> cache;
	private final MeterRegistry meterRegistry;

	public CacheTokenClient(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
		this.cache = Caffeine.newBuilder()
				.expireAfterWrite(50, TimeUnit.MINUTES)
				.maximumSize(1000)
				.build();
	}

	@Override
	public String getTokenFromCacheOrProvider(String cacheKey, Supplier<String> tokenProvider) {
		JwtToken cacheJwtToken = cache.getIfPresent(cacheKey);
		JwtToken tokenFromProvider = new JwtToken(tokenProvider.get());

		if (expiresWithin(cacheJwtToken)) {
			cache.put(cacheKey, tokenFromProvider);
			incrementCacheTokenCounter(cacheKey, tokenFromProvider.getSubject(), tokenFromProvider.getIssuer());
			return tokenFromProvider.getTokenAsString();
		}
		return cacheJwtToken.getTokenAsString();
	}

	public boolean expiresWithin(JwtToken jwtToken) {
		if (isNull(jwtToken)) {
			return true;
		}

		Date tokenExpirationTime = jwtToken.getJwtTokenClaims().getExpirationTime();

		if (isNull(tokenExpirationTime)) {
			return true;
		}

		long expirationTimeWithWindow = tokenExpirationTime.getTime() - ofSeconds(30).getSeconds();

		return System.currentTimeMillis() > expirationTimeWithWindow;
	}

	private void incrementCacheTokenCounter(final String cacheKey, final String subject, final String issuer) {
		Counter.builder("dok_request_cache_token")
				.tags("cacheKey", cacheKey)
				.tags("subject", subject)
				.tags("issuer", issuer)
				.register(meterRegistry)
				.increment();
	}
}
