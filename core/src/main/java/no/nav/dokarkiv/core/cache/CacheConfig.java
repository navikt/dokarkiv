package no.nav.dokarkiv.core.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

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

	@Bean
	CacheManager cacheManager() {
		SimpleCacheManager manager = new SimpleCacheManager();
		manager.setCaches(Arrays.asList(
				new CaffeineCache(NAVUSER_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(8, TimeUnit.HOURS)
						.maximumSize(10000)
						.build()),
				new CaffeineCache(NAVSERVICEUSER_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(2, TimeUnit.DAYS)
						.maximumSize(10000)
						.build()),
				new CaffeineCache(USERNAME_TOKEN_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(10, TimeUnit.MINUTES)
						.maximumSize(10)
						.build()),
				new CaffeineCache(REST_STS_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(50, TimeUnit.MINUTES)
						.maximumSize(1)
						.build())
		));
		return manager;
	}
}
