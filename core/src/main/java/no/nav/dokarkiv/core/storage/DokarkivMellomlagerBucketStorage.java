package no.nav.dokarkiv.core.storage;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import no.nav.dokarkiv.core.exceptions.DokarkivTechnicalException;

import java.io.IOException;
import java.io.InputStream;

/// Laster opp dokumentfiler til dokarkivmellomlager i Google Cloud Storage.
public class DokarkivMellomlagerBucketStorage implements BucketStorageOperations {

	private final String bucket;
	private final Storage storage;

	public DokarkivMellomlagerBucketStorage(Storage storage, String bucket) {
		this.storage = storage;
		this.bucket = bucket;
	}

	public OpplastetDokumentFil uploadObject(String objectName, InputStream inputStream, String contentType) {
		try {
			BlobId blobId = BlobId.of(bucket, objectName);
			BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType).build();
			Blob blob = storage.createFrom(blobInfo, inputStream);
			return new OpplastetDokumentFil(blob.getCrc32c(), blob.getSize());
		} catch (IOException | RuntimeException e) {
			throw new DokarkivTechnicalException(String.format("Feilet ved opplasting av dokument med objectName=%s til Google Cloud Storage.", objectName), e);
		}
	}
}
