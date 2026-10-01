package no.nav.dokarkiv.core.util;

import org.jspecify.annotations.NonNull;

import java.util.Base64;
import java.util.zip.CRC32C;

/// Hjelpemetoder for å beregne crc32c-sjekksummer på samme format som Google Cloud Storage bruker
/// i objektmetadata (big-endian 4-byte verdi, base64-kodet).
public final class Crc32c {

	private Crc32c() {
		// ingen instansiering
	}

	public static byte[] digest(byte[] data) {
		CRC32C crc32c = new CRC32C();
		crc32c.update(data);

		int crcInt = (int) crc32c.getValue();
		return getBytes(crcInt);
	}

	public static String base64(byte[] data) {
		CRC32C crc32c = new CRC32C();
		crc32c.update(data);
		return base64(crc32c.getValue());
	}

	public static String base64(long crc32cValue) {
		int crcInt = (int) crc32cValue;

		return Base64.getEncoder().encodeToString(getBytes(crcInt));
	}

	private static byte @NonNull [] getBytes(int crcInt) {
		return new byte[]{
				(byte) (crcInt >>> 24),
				(byte) (crcInt >>> 16),
				(byte) (crcInt >>> 8),
				(byte) crcInt
		};
	}
}
