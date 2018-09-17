package no.nav.dokarkiv.core.storage;

import static java.util.stream.Collectors.joining;
import static no.nav.dokarkiv.core.storage.config.StorageConfiguration.BUCKET_NAME;
import static no.nav.dokarkiv.core.util.RetryConstants.DELAY_SHORT;
import static no.nav.dokarkiv.core.util.RetryConstants.MAX_ATTEMPTS_SHORT;
import static no.nav.dokarkiv.core.util.RetryConstants.MULTIPLIER_SHORT;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3Object;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.exceptions.DokarkivTechnicalException;
import no.nav.dokarkiv.core.storage.crypto.Crypto;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Optional;

@Slf4j
public class S3Storage implements Storage {

	private AmazonS3 s3;
	private String encryptionPassphrase;

	@Inject
	public S3Storage(AmazonS3 s3, String encryptionPassphrase) {
		this.s3 = s3;
		this.encryptionPassphrase = encryptionPassphrase;
	}

	@Override
	@Retryable(include = DokarkivTechnicalException.class, maxAttempts = MAX_ATTEMPTS_SHORT, backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT))
	public void put(String directory, String key, String value) {
		try {
			String encryptedValue = encrypt(value, key);
			writeString(directory, key, encryptedValue);
		} catch (Exception e) {
			throw new DokarkivTechnicalException(String.format("Feilet ved sending av dokument til S3. Nøkkel=%s", key), e);
		}

	}

	@Override
	@Retryable(include = DokarkivTechnicalException.class, maxAttempts = MAX_ATTEMPTS_SHORT, backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT))
	public Optional<String> get(String directory, String key) {

		try {
			String encryptedValue = readString(directory, key);
			return Optional.ofNullable(decrypt(encryptedValue, key));
		} catch (Exception e) {
			throw new DokarkivTechnicalException(String.format("Feilet ved henting av dokument fra S3. Nøkkel=%s", key), e);
		}

	}

	@Override
	public void delete(String directory, String key) {
		deleteString(directory, key);
	}

	//TODO Add timer?
	private void writeString(String directory, String key, String value) {
		s3.putObject(BUCKET_NAME, fileName(directory, key), value);
	}

	//TODO Add timer?
	private String readString(String directory, String key) {
		String path = fileName(directory, key);
		S3Object object;
		try {
			object = s3.getObject(BUCKET_NAME, path);
		} catch (AmazonS3Exception ex) {
			log.warn("Unable to retrieve " + path + ", it probably doesn't exist");
			return null;
		}

		return new BufferedReader(new InputStreamReader(object.getObjectContent()))
				.lines()
				.collect(joining("\n"));

	}

	//TODO Add timer?
	private void deleteString(String directory, String key) {
		s3.deleteObject(BUCKET_NAME, fileName(directory, key));
	}

	private String fileName(String directory, String key) {
		return directory + "_" + key;
	}

	private String encrypt(String plaintext, String key) {
		return new Crypto(encryptionPassphrase, key).encrypt(plaintext);
	}

	private String decrypt(String encrypted, String key) {
		return new Crypto(encryptionPassphrase, key).decrypt(encrypted);
	}

}
