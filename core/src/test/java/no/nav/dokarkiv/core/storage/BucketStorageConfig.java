package no.nav.dokarkiv.core.storage;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.mockito.Mockito.mock;

@Configuration
public class BucketStorageConfig {

	@Bean
	public BucketStorage storage() {
		return mock(GoogleCloudBucketStorage.class);
	}
}
