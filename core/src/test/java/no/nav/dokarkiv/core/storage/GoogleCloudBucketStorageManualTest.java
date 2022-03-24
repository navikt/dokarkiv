package no.nav.dokarkiv.core.storage;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class GoogleCloudBucketStorageManualTest {

	@Test
	@Disabled
	void shouldDownload() {
		String objectName="e6ac2ef6-3752-4dd1-b55f-a72b451cb4da_15500_1";

		BucketStorage bucketStorage = new GoogleCloudStorageConfiguration().setUpStorage(
				System.getProperty("projectId"),
				System.getProperty("bucket"),
				System.getProperty("encryptionPassphrase")
		);
		Optional<String> downloadedObject = bucketStorage.downloadObject(objectName);
	}
}
