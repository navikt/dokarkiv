package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;

import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

/// @param crc32c Den dekodede crc32c-verdien fra digest-value (4 bytes, big-endian).
public record Crc32cContentDigest(byte[] crc32c) {

	private static final Pattern CONTENT_DIGEST_PATTERN = Pattern.compile("^crc32c=:([A-Za-z0-9+/]{6}==):$");

	public static Crc32cContentDigest validateAndParse(String contentDigestHeader) {
		Matcher matcher = CONTENT_DIGEST_PATTERN.matcher(contentDigestHeader);
		if (!matcher.matches()) {
			throw new InputValideringFeiletException(format(
					"Header Content-Digest=%s har ugyldig format. Forventet format er crc32c=:{base64-enkodet-crc32c-digest-big-endian}:", contentDigestHeader));
		}

		byte[] crc32c;
		try {
			crc32c = Base64.getDecoder().decode(matcher.group(1));
		} catch (IllegalArgumentException e) {
			throw new InputValideringFeiletException(format(
					"Header Content-Digest=%s inneholder en digest-value som ikke er gyldig base64", contentDigestHeader));
		}

		return new Crc32cContentDigest(crc32c);
	}

	public String base64() {
		return Base64.getEncoder().encodeToString(crc32c);
	}
}
