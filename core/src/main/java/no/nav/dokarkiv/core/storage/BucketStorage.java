package no.nav.dokarkiv.core.storage;

import java.util.Optional;

public interface BucketStorage {
	/**
	 * Laster ned kryptert payload fra ekstern bucket i Google Cloud Storage
	 *
	 * @param objectName Navn på objekt
	 */
	Optional<String> downloadObject(String objectName);
}
