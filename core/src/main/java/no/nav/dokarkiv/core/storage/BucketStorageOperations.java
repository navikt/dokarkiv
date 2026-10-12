package no.nav.dokarkiv.core.storage;

import java.io.InputStream;
import java.util.Collection;
import java.util.Set;

/// Laster opp objekter til bucket
public interface BucketStorageOperations {
	/// Streamer payload til bucket
	///
	/// @param objectName  Navn på objektet som skal opprettes i bucket. Må være en unik ID, f.eks. dokumentfilId.
	/// @param inputStream Payloaden som skal lastes opp.
	/// @param contentType Content-Type til payloaden.
	/// @return crc32c-sjekksum og antall bytes for det opplastede objektet.
	OpplastetDokumentFil uploadObject(String objectName, InputStream inputStream, String contentType);

	/// Sjekker i ett batch-kall hvilke objectNames som eksisterer i bucket
	///
	/// @param objectNames navn på objektene som skal sjekkes.
	/// @return objectNames som finnes i bucket.
	Set<String> existsObjects(Collection<String> objectNames);

	/// Setter temporary hold på objectNames slik at de ikke kan slettes (f.eks. av lifecycle) eller endres.
	/// Objekter som allerede har hold blir ikke oppdatert på nytt.
	///
	/// @param objectNames navn på objektene som skal ha hold.
	/// @return antall objekter med hold
	int holdObjects(Collection<String> objectNames);
}
