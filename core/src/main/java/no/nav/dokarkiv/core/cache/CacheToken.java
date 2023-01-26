package no.nav.dokarkiv.core.cache;

import java.util.function.Supplier;

public interface CacheToken {
	String getTokenFromCacheOrProvider(String cacheKey, Supplier<String> tokenProvider);
}
