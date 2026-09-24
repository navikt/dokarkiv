package no.nav.dokarkiv.core.storage;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DokarkivMellomlagerTestBucketConfiguration {

	@Bean
	public InMemoryBucketStorage dokarkivMellomlagerBucketStorage() {
		return new InMemoryBucketStorage();
	}
}
