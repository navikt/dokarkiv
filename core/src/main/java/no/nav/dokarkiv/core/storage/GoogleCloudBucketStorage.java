package no.nav.dokarkiv.core.storage;

import com.google.cloud.storage.Storage;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.exceptions.DokarkivTechnicalException;
import no.nav.dokarkiv.core.storage.crypto.Crypto;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import java.util.Optional;

import static no.nav.dokarkiv.core.storage.RetryConstants.DELAY_SHORT;
import static no.nav.dokarkiv.core.storage.RetryConstants.MULTIPLIER_SHORT;

@Slf4j
public class GoogleCloudBucketStorage implements BucketStorage {

	private final String bucket;
	private final Storage storage;
	private final String encryptionPassphrase;

	public GoogleCloudBucketStorage(Storage storage, String bucket, String encryptionPassphrase) {
		this.storage = storage;
		this.bucket = bucket;
		this.encryptionPassphrase = encryptionPassphrase;
	}

	@Override
	@Retryable(
			include = DokarkivTechnicalException.class,
			backoff = @Backoff(delay = DELAY_SHORT,
			multiplier = MULTIPLIER_SHORT)
	)
	public Optional<String> downloadObject(String objectName) {
		try {
			byte[] encryptedValue = storage.readAllBytes(bucket, objectName);
			String encryptedValuesAsString = new String(encryptedValue);

			return Optional.of(decrypt(encryptedValuesAsString, objectName));
		} catch (Exception e) {
			throw new DokarkivTechnicalException(String.format("Feilet ved henting av dokument med objectName=%s fra Google Cloud Storage.", objectName), e);
		}
	}

	private String decrypt(String encrypted, String key) {
		return new Crypto(encryptionPassphrase, key).decrypt(encrypted);
	}

}
