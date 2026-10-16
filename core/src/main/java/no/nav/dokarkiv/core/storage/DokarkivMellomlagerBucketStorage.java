package no.nav.dokarkiv.core.storage;

import com.google.cloud.BatchResult;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.BlobTargetOption;
import com.google.cloud.storage.StorageBatch;
import com.google.cloud.storage.StorageException;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.exceptions.DokarkivTechnicalException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/// Laster opp dokumentfiler til dokarkivmellomlager i Google Cloud Storage.
@Slf4j
public class DokarkivMellomlagerBucketStorage implements BucketStorageOperations {

	// Maks antall operasjoner i en GCS batch-request
	private static final int MAKS_BATCH_STORRELSE = 100;

	private final String bucket;
	private final Storage storage;

	public DokarkivMellomlagerBucketStorage(Storage storage, String bucket) {
		this.storage = storage;
		this.bucket = bucket;
	}

	@Override
	public OpplastetDokumentFil uploadObject(String objectName, InputStream inputStream, String contentType) {
		try {
			BlobId blobId = BlobId.of(bucket, objectName);
			BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType).build();
			Blob blob = storage.createFrom(blobInfo, inputStream);
			return new OpplastetDokumentFil(blob.getCrc32c(), blob.getSize());
		} catch (IOException | RuntimeException e) {
			throw new DokarkivTechnicalException(String.format("Feilet ved opplasting av dokument med objectName=%s til dokarkivmellomlager.", objectName), e);
		}
	}

	@Override
	public Set<String> existsObjects(Collection<String> objectNames) {
		if (objectNames.isEmpty()) {
			return Set.of();
		}

		try {
			List<BlobId> blobIds = objectNames.stream()
					.map(objectName -> BlobId.of(bucket, objectName))
					.toList();
			// Batch-oppslag. Returnerer null for objekter som ikke finnes.
			List<Blob> blobs = storage.get(blobIds);
			return blobs.stream()
					.filter(Objects::nonNull)
					.filter(Blob::exists)
					.map(Blob::getName)
					.collect(Collectors.toSet());
		} catch (RuntimeException e) {
			throw new DokarkivTechnicalException(String.format("Feilet ved sjekk om blobs med objectNames=%s eksisterer i dokarkivmellomlager.", objectNames), e);
		}
	}

	@Override
	public int holdObjects(Collection<String> objectNames) {
		if (objectNames.isEmpty()) {
			return 0;
		}

		List<String> objectNameListe = List.copyOf(objectNames);
		try {
			// Batch-oppslag. Returnerer null for objekter som ikke finnes, i samme rekkefølge som input.
			List<Blob> blobs = storage.get(objectNameListe.stream()
					.map(objectName -> BlobId.of(bucket, objectName))
					.toList());

			List<String> mangler = IntStream.range(0, blobs.size())
					.filter(i -> blobs.get(i) == null || !blobs.get(i).exists())
					.mapToObj(objectNameListe::get)
					.toList();
			if (!mangler.isEmpty()) {
				throw new DokarkivTechnicalException(String.format("Kan ikke sette hold på blobs med objectNames=%s. De finnes ikke i dokarkivmellomlager.", mangler));
			}

			List<Blob> utenHold = blobs.stream()
					.filter(blob -> !Boolean.TRUE.equals(blob.getTemporaryHold()))
					.toList();
			if (utenHold.size() < blobs.size()) {
				log.info("Hopper over {} blobs som allerede har hold i bucket", blobs.size() - utenHold.size());
			}

			Set<String> feilet = ConcurrentHashMap.newKeySet();
			for (int fra = 0; fra < utenHold.size(); fra += MAKS_BATCH_STORRELSE) {
				holdBatch(utenHold.subList(fra, Math.min(fra + MAKS_BATCH_STORRELSE, utenHold.size())), feilet);
			}

			if (!feilet.isEmpty()) {
				throw new DokarkivTechnicalException(String.format("Feilet ved setting av hold på blobs med objectNames=%s i dokarkivmellomlager.", feilet));
			}
			return utenHold.size();
		} catch (DokarkivTechnicalException e) {
			throw e;
		} catch (RuntimeException e) {
			throw new DokarkivTechnicalException(String.format("Feilet ved setting av hold på blobs med objectNames=%s i dokarkivmellomlager.", objectNames), e);
		}
	}

	private void holdBatch(List<Blob> blobs, Set<String> feilet) {
		StorageBatch batch = storage.batch();
		blobs.forEach(blob -> {
			BlobInfo medHold = BlobInfo.newBuilder(blob.getBlobId())
					.setTemporaryHold(true)
					.build();
			// generationMatch sikrer at hold kun settes på den versjonen av objektet som ble validert
			batch.update(medHold, BlobTargetOption.generationMatch(blob.getGeneration()))
					.notify(new BatchResult.Callback<>() {
						@Override
						public void success(Blob result) {
						}

						@Override
						public void error(StorageException exception) {
							log.warn("Feilet ved setting av hold på blob med objectName={}", blob.getName(), exception);
							feilet.add(blob.getName());
						}
					});
		});
		batch.submit();
	}
}
