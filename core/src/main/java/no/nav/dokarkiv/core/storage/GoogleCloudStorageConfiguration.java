package no.nav.dokarkiv.core.storage;

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
public class GoogleCloudStorageConfiguration {

	@Bean
	@Lazy
	public GoogleCloudBucketStorage setUpStorage(
			@Value("${dokprodmellomlager.projectid}") String projectId,
			@Value("${dokprodmellomlager.bucket}") String bucket,
			@Value("${dokprodmellomlager_s3_storage_crypto_password}") String encryptionPassphrase
	) {
		Storage storage = StorageOptions.newBuilder()
				.setProjectId(projectId)
				.setTransportOptions(StorageOptions.getDefaultHttpTransportOptions().toBuilder()
						.setConnectTimeout((int) SECONDS.toMillis(5))
						.setReadTimeout((int) SECONDS.toMillis(20))
						.build())
				.build().getService();

		return new GoogleCloudBucketStorage(storage, bucket, encryptionPassphrase);
	}
}
