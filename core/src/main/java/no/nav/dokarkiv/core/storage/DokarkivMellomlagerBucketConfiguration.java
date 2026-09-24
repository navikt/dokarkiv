package no.nav.dokarkiv.core.storage;

import com.google.cloud.storage.HttpStorageOptions;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;

import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
@Configuration
@Profile("nais")
public class DokarkivMellomlagerBucketConfiguration {

	@Bean
	@Lazy
	public DokarkivMellomlagerBucketStorage dokarkivMellomlagerBucketStorage(
			@Value("${dokarkivmellomlager.projectid}") String projectId,
			@Value("${dokarkivmellomlager.bucket}") String bucket
	) {
		Storage storage = createStorage(projectId);
		return new DokarkivMellomlagerBucketStorage(storage, bucket);
	}

	Storage createStorage(String projectId) {
		return StorageOptions.newBuilder()
				.setProjectId(projectId)
				.setTransportOptions(HttpStorageOptions.defaults().getDefaultTransportOptions().toBuilder()
						.setConnectTimeout((int) SECONDS.toMillis(5))
						.setReadTimeout((int) SECONDS.toMillis(180))
						.build())
				.build().getService();
	}
}
