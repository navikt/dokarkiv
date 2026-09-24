package no.nav.dokarkiv.core.storage;

import java.io.InputStream;

/// Laster opp objekter til bucket
public interface BucketStorageOperations {
	/// Streamer payload til bucket
	///
	/// @param objectName  Navn på objektet som skal opprettes i bucket. Må være en unik ID, f.eks. dokumentfilId.
	/// @param inputStream Payloaden som skal lastes opp.
	/// @param contentType Content-Type til payloaden.
	/// @return crc32c-sjekksum og antall bytes for det opplastede objektet.
	OpplastetDokumentFil uploadObject(String objectName, InputStream inputStream, String contentType);
}
