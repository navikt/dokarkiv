package no.nav.dokarkiv.core.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.cache.CaffeineCacheMetrics;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MINUTES;

@Configuration
@EnableCaching
public class CacheConfig {

	public static final String USERNAME_TOKEN_CACHE = "usernameTokenCache";
	public static final String NAVUSER_CACHE = "navuserCache";
	public static final String NAVSERVICEUSER_CACHE = "navserviceuserCache";
	public static final String HISTORISKE_IDENTER = "historiskeIdenterCache";
	public static final String FAGOMRADE_CACHE = "fagomradeCache";
	public static final String AZURE_CLIENT_CREDENTIAL_GRAPH_TOKEN_CACHE = "azureClientCredentialGraphTokeCache";
	public static final String AZURE_ON_BEHALF_OF_TOKEN_CACHE = "hentOnBehalfOfCache";
	public static final String SAF_JOURNALPOST_QUERY_CACHE = "safJournalpostQueryCache";

	private final MeterRegistry meterRegistry;

	public CacheConfig(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
	}

	@Bean
	CacheManager cacheManager() {
		CaffeineCacheManager manager = new CaffeineCacheManager();
		caffeineCaches().forEach(caffeineCache -> {
					manager.registerCustomCache(caffeineCache.getName(), caffeineCache.getNativeCache());
					CaffeineCacheMetrics.monitor(meterRegistry, caffeineCache.getNativeCache(), caffeineCache.getName());
				}
		);
		return manager;
	}

	private List<CaffeineCache> caffeineCaches() {
		return Arrays.asList(
				new CaffeineCache(NAVUSER_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(8, HOURS)
						.maximumSize(10000)
						.recordStats()
						.build()),
				new CaffeineCache(NAVSERVICEUSER_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(2, DAYS)
						.maximumSize(10000)
						.recordStats()
						.build()),
				new CaffeineCache(USERNAME_TOKEN_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(10, MINUTES)
						.maximumSize(10)
						.recordStats()
						.build()),
				new CaffeineCache(HISTORISKE_IDENTER, Caffeine.newBuilder()
						.expireAfterWrite(10, MINUTES)
						.maximumSize(25000)
						.build()),
				new CaffeineCache(AZURE_CLIENT_CREDENTIAL_GRAPH_TOKEN_CACHE,
						Caffeine.newBuilder()
								.expireAfterWrite(50, MINUTES)
								.maximumSize(10)
								.recordStats()
								.build()),
				new CaffeineCache(AZURE_ON_BEHALF_OF_TOKEN_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(50, MINUTES)
						.maximumSize(10000)
						.recordStats()
						.build()),
				new CaffeineCache(SAF_JOURNALPOST_QUERY_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(10, MINUTES)
						.maximumSize(1000)
						.recordStats()
						.build()),
				new CaffeineCache(FAGOMRADE_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(24, HOURS)
						.maximumSize(FagomradeCode.values().length)
						.recordStats()
						.build()));
	}
}
