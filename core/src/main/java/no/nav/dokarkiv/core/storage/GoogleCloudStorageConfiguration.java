package no.nav.dokarkiv.core.storage;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.crypto.tink.Aead;
import com.google.crypto.tink.KeyTemplates;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.KmsEnvelopeAeadKeyManager;
import com.google.crypto.tink.integration.gcpkms.GcpKmsClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;

import java.security.GeneralSecurityException;

import static java.util.Optional.empty;
import static java.util.Optional.of;
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
			@Value("${dokprodmellomlager.keyring}") String keyring,
			@Value("${dokprodmellomlager.keyid}") String keyid
	) throws Exception {

		Storage storage = configureGcpStorage(projectId);
		Aead aead = configureDecryption(projectId, keyring, keyid);

		return new GoogleCloudBucketStorage(storage, bucket, aead);
	}

	// Dekryptering blir utført med Tink og nøkler håndtert av GCP KMS
	private Aead configureDecryption(String projectId, String keyring, String keyid) throws GeneralSecurityException {
		AeadConfig.register();
		final String kekUri = getKekUri(projectId, keyring, keyid);
		GcpKmsClient.register(of(kekUri), empty());
		return KeysetHandle
				.generateNew(KmsEnvelopeAeadKeyManager.createKeyTemplate(kekUri, KeyTemplates.get("AES128_GCM")))
				.getPrimitive(Aead.class);
	}

	private String getKekUri(String projectId, String keyring, String keyid) {
		return "gcp-kms://projects/" + projectId + "/locations/europe-north1/keyRings/" + keyring + "/cryptoKeys/" + keyid;
	}

	private Storage configureGcpStorage(String projectId) {
		return StorageOptions.newBuilder()
				.setProjectId(projectId)
				.setTransportOptions(StorageOptions.getDefaultHttpTransportOptions().toBuilder()
						.setConnectTimeout((int) SECONDS.toMillis(5))
						.setReadTimeout((int) SECONDS.toMillis(20))
						.build())
				.build().getService();
	}
}
