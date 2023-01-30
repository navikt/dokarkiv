package no.nav.dokarkiv.core.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Configuration
@EnableCaching
public class CacheConfig {

	public static final String USERNAME_TOKEN_CACHE = "usernameTokenCache";
	public static final String NAVUSER_CACHE = "navuserCache";
	public static final String NAVSERVICEUSER_CACHE = "navserviceuserCache";
	public static final String REST_STS_CACHE = "RESTSTS";
	public static final String HISTORISKE_IDENTER = "historiskeIdenterCache";
	public static final String AZURE_CLIENT_CREDENTIAL_GRAPH_TOKEN_CACHE = "azureClientCredentialGraphTokeCache";
	public static final String AZURE_HENT_AD_GRUPPER = "hentAdGrupperCache";
	public static final String AZURE_ON_BEHALF_OF_TOKEN_CACHE = "hentAdGrupperCache";

	@Bean
	CacheManager cacheManager() {
		SimpleCacheManager manager = new SimpleCacheManager();
		manager.setCaches(Arrays.asList(
				new CaffeineCache(NAVUSER_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(8, HOURS)
						.maximumSize(10000)
						.build()),
				new CaffeineCache(NAVSERVICEUSER_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(2, DAYS)
						.maximumSize(10000)
						.build()),
				new CaffeineCache(USERNAME_TOKEN_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(10, MINUTES)
						.maximumSize(10)
						.build()),
				new CaffeineCache(REST_STS_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(50, MINUTES)
						.maximumSize(1)
						.build()),
				new CaffeineCache(HISTORISKE_IDENTER, Caffeine.newBuilder()
						.expireAfterWrite(10, MINUTES)
						.maximumSize(25000)
						.build()),
				new CaffeineCache(AZURE_CLIENT_CREDENTIAL_GRAPH_TOKEN_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(50, MINUTES)
						.maximumSize(10)
						.build()),
				new CaffeineCache(AZURE_ON_BEHALF_OF_TOKEN_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(50, MINUTES)
						.maximumSize(10000)
						.build()),
				new CaffeineCache(AZURE_HENT_AD_GRUPPER, Caffeine.newBuilder()
						.expireAfterWrite(10, MINUTES)
						.maximumSize(10000)
						.build())));
		return manager;
	}
}
