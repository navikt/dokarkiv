package no.nav.dokarkiv.core.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Digest {

	private Digest() {
		// ingen instansiering
	}

	public static byte[] sha256(byte[] data) {
		try {
			return MessageDigest.getInstance("SHA-256").digest(data);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalArgumentException(e);
		}
	}
}
