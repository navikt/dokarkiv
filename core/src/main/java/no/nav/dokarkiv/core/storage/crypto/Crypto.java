package no.nav.dokarkiv.core.storage.crypto;

import no.nav.dokarkiv.core.exceptions.CryptoException;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.spec.KeySpec;
import java.util.Base64;

public class Crypto {

	private final SecretKey key;
	private String iv;

	private static final String ALGO = "AES/CBC/PKCS5Padding";

	public Crypto(String passphrase, String storageKey) {
		if (isEmpty(passphrase) || isEmpty(storageKey)) {
			throw new IllegalArgumentException("Both passphrase and storageKey must be provided");
		}
		key = key(passphrase, storageKey);

		iv = storageKey;
		// iv must be 16 bytes
		if (iv.length() < 16) {
			iv = iv + StringUtils.repeat("0", 16 - iv.length());
		} else if (iv.length() > 16) {
			iv = iv.substring(0, 16);
		}
	}

	public String encrypt(String plainText) {
		try {
			Cipher cipher = Cipher.getInstance(ALGO);
			cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv.getBytes()));
			return Base64.getEncoder().encodeToString(cipher.doFinal(plainText.getBytes()));
		} catch (Exception ex) {
			throw new CryptoException("Feilet ved kryptering av tekst", ex);
		}
	}

	public String decrypt(String encrypted) {
		try {
			Cipher cipher = Cipher.getInstance(ALGO);
			cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv.getBytes()));
			return new String(cipher.doFinal(Base64.getDecoder().decode(encrypted)));
		} catch (Exception ex) {
			throw new CryptoException("Feilet ved dekryptering av tekst", ex);
		}
	}

	private SecretKey key(String passphrase, String salt) {
		try {
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			char[] ***passord=gammelt_passord***();
			KeySpec spec = new PBEKeySpec(passwordChars, salt.getBytes(), 10000, 128);
			SecretKey key = factory.generateSecret(spec);
			return new SecretKeySpec(key.getEncoded(), "AES");
		} catch (Exception ex) {
			throw new CryptoException("Feilet ved generering av krypteringsnøkkel", ex);
		}
	}

	private boolean isEmpty(String str) {
		return str == null || str.trim().length() == 0;
	}

}
