package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;

import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

/// Representerer en verifisert `Content-Digest`-header med sha-256 som digest-algoritme, slik den
/// kreves av LastOppDokumentfil-tjenesten. Se RFC 9530 for bakgrunn om Content-Digest-headeren.
///
/// @param sha256Sjekksum Den dekodede sha256-verdien fra digest-value.
public record Sha256ContentDigest(byte[] sha256Sjekksum) {

	private static final int SHA256_LENGTH_BYTES = 32;
	private static final Pattern CONTENT_DIGEST_PATTERN = Pattern.compile("^sha-256=:([A-Za-z0-9+/]{43}=):$");

	/// Parser og validerer en `Content-Digest`-header.
	///
	/// @param contentDigestHeader verdien til Content-Digest-headeren.
	/// @return Et gyldig [Sha256ContentDigest].
	/// @throws InputValideringFeiletException hvis headeren ikke har sha-256 som digest-algoritme, eller digest-value
	///                                                                                                                       ikke har formkravene til en base64-kodet sha256-sjekksum.
	public static Sha256ContentDigest validateAndParse(String contentDigestHeader) {
		Matcher matcher = CONTENT_DIGEST_PATTERN.matcher(contentDigestHeader);
		if (!matcher.matches()) {
			throw new InputValideringFeiletException(format(
					"Header Content-Digest=%s har ugyldig format. Forventet format er sha-256=:<base64-kodet sha256-sjekksum>:", contentDigestHeader));
		}

		byte[] sha256Sjekksum;
		try {
			sha256Sjekksum = Base64.getDecoder().decode(matcher.group(1));
		} catch (IllegalArgumentException e) {
			throw new InputValideringFeiletException(format(
					"Header Content-Digest=%s inneholder en digest-value som ikke er gyldig base64", contentDigestHeader));
		}

		return new Sha256ContentDigest(sha256Sjekksum);
	}

	public String base64() {
		return Base64.getEncoder().encodeToString(sha256Sjekksum);
	}
}
