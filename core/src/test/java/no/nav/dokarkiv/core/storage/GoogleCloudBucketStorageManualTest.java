package no.nav.dokarkiv.core.storage;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class GoogleCloudBucketStorageManualTest {

	/*
	For manuell testing av nedlasting fra Google Cloud Storage bucket

	Legg inn objectName til filen du vil laste ned i shouldDownload

	Legg til dette i VM Options i klassa (Edit configurations) i IntelliJ
		-DprojectId=teamdokumenthandteri-dev-2c5a
		-Dbucket=dokprodmellomlager-dev
		-Dkeyring=dokprodmellomlager-keyring-dev
		-Dkeyid=dokprodmellomlager-symmetric-dev

	I tillegg må dette inn i Environment variables
		name: GOOGLE_APPLICATION_CREDENTIALS
		Gå til dokarkiv i vault (preprod-fss) og kopier ut det som ligger i gcloud_serviceaccount til en .json-fil.
		value: Stien til .json-filen

	*/
	@Test
	@Disabled
	void shouldDownload() throws Exception {
		String objectName="manual-b23a4e41-cb87-470e-b10c-bf8e7daa256c";

		BucketStorage bucketStorage = new GoogleCloudStorageConfiguration().setUpStorage(
				System.getProperty("projectId"),
				System.getProperty("bucket"),
				System.getProperty("keyring"),
				System.getProperty("keyid")
		);
		Optional<String> downloadedObject = bucketStorage.downloadObject(objectName);
	}
}
