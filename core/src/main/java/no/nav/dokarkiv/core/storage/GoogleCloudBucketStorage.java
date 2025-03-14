package no.nav.dokarkiv.core.storage;

import com.google.cloud.storage.Storage;
import com.google.crypto.tink.Aead;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.exceptions.DokarkivTechnicalException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import java.util.Optional;

import static no.nav.dokarkiv.core.storage.RetryConstants.DELAY_SHORT;
import static no.nav.dokarkiv.core.storage.RetryConstants.MULTIPLIER_SHORT;

@Slf4j
public class GoogleCloudBucketStorage implements BucketStorage {

	private final String bucket;
	private final Storage storage;
	private final Aead aead;

	public GoogleCloudBucketStorage(Storage storage, String bucket, Aead aead) {
		this.storage = storage;
		this.bucket = bucket;
		this.aead = aead;
	}

	@Override
	@Retryable(
			retryFor = DokarkivTechnicalException.class,
			backoff = @Backoff(delay = DELAY_SHORT,
			multiplier = MULTIPLIER_SHORT)
	)
	public Optional<String> downloadObject(String objectName, String associatedData) {
		try {
			byte[] cipherText = storage.readAllBytes(bucket, objectName);
			byte[] plainText = aead.decrypt(cipherText, associatedData.getBytes());

			return Optional.of(new String(plainText));
		} catch (Exception e) {
			throw new DokarkivTechnicalException(String.format("Feilet ved henting av dokument med objectName=%s fra Google Cloud Storage.", objectName), e);
		}
	}

}
