package no.nav.dokarkiv.core.storage;

import static java.util.stream.Collectors.joining;
import static no.nav.dokarkiv.core.storage.RetryConstants.DELAY_SHORT;
import static no.nav.dokarkiv.core.storage.RetryConstants.MAX_ATTEMPTS_SHORT;
import static no.nav.dokarkiv.core.storage.RetryConstants.MULTIPLIER_SHORT;

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
public class DokprodMellomlagerS3Storage implements Storage {
	static final String DOKPRODMELLOMLAGER_BUCKET = "dokprodmellomlager";
	public static final String DOKPRODMELLOMLAGER_DIRECTORY_NAME = "dokprod";

	private final AmazonS3 s3;
	private final String encryptionPassphrase;

	@Inject
	public DokprodMellomlagerS3Storage(AmazonS3 s3, String encryptionPassphrase) {
		this.s3 = s3;
		this.encryptionPassphrase = encryptionPassphrase;
	}

	@Override
	public void put(String directory, String key, String value) {
		throw new UnsupportedOperationException("Det er ikke støttet å legge til objekter i dokprodmellomlager bucketen");
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
		throw new UnsupportedOperationException("Det er ikke støttet å slette objekter i dokprodmellomlager bucketen");
	}

	private String readString(String directory, String key) {
		String path = fileName(directory, key);
		S3Object object;
		try {
			object = s3.getObject(DOKPRODMELLOMLAGER_BUCKET, path);
		} catch (AmazonS3Exception ex) {
			log.warn("Unable to retrieve " + path + ", it probably doesn't exist");
			return null;
		}

		return new BufferedReader(new InputStreamReader(object.getObjectContent()))
				.lines()
				.collect(joining("\n"));
	}

	private String fileName(String directory, String key) {
		return directory + "_" + key;
	}

	private String decrypt(String encrypted, String key) {
		return new Crypto(encryptionPassphrase, key).decrypt(encrypted);
	}

}
