package no.nav.dokarkiv.core.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static no.nav.dokarkiv.core.util.Crc32c.base64;

/// Test-implementasjon av [BucketStorageOperations] som lagrer objekter i minnet
/// og beregner crc32c på samme måte som Google Cloud Storage ville gjort. Brukes i integrasjonstester
/// i stedet for en ekte GCS-bucket.
public class InMemoryBucketStorage implements BucketStorageOperations {

	private final Map<String, byte[]> objects = new ConcurrentHashMap<>();

	@Override
	public OpplastetDokumentFil uploadObject(String objectName, InputStream inputStream, String contentType) {
		try {
			byte[] bytes = inputStream.readAllBytes();
			objects.put(objectName, bytes);
			return new OpplastetDokumentFil(base64(bytes), bytes.length);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public byte[] getObject(String objectName) {
		return objects.get(objectName);
	}

	public void clear() {
		objects.clear();
	}
}
