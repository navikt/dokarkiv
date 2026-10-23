package no.nav.dokarkiv.core.storage;

import no.nav.dokarkiv.core.exceptions.DokarkivTechnicalException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static no.nav.dokarkiv.core.util.Crc32c.base64;

/// Test-implementasjon av [BucketStorageOperations] som lagrer objekter i minnet
/// og beregner crc32c på samme måte som Google Cloud Storage ville gjort. Brukes i integrasjonstester
/// i stedet for en ekte GCS-bucket.
public class InMemoryBucketStorage implements BucketStorageOperations {

	private final Map<String, byte[]> objects = new ConcurrentHashMap<>();
	private final Set<String> heldObjects = ConcurrentHashMap.newKeySet();

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

	@Override
	public Set<String> existsObjects(Collection<String> objectNames) {
		return objectNames.stream()
				.filter(objects::containsKey)
				.collect(Collectors.toSet());
	}

	@Override
	public int holdObjects(Collection<String> objectNames) {
		List<String> mangler = objectNames.stream()
				.filter(objectName -> !objects.containsKey(objectName))
				.toList();
		if (!mangler.isEmpty()) {
			throw new DokarkivTechnicalException(String.format("Kan ikke sette hold på blobs med objectNames=%s. De finnes ikke.", mangler));
		}
		heldObjects.addAll(objectNames);
		return objectNames.size();
	}

	public boolean isHeld(String objectName) {
		return heldObjects.contains(objectName);
	}

	public byte[] getObject(String objectName) {
		return objects.get(objectName);
	}

	public void clear() {
		objects.clear();
		heldObjects.clear();
	}
}
