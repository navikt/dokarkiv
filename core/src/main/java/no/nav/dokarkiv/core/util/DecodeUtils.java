package no.nav.dokarkiv.core.util;

import org.springframework.security.authentication.BadCredentialsException;

import java.io.IOException;
import java.util.Base64;

/**
 * @author Olav Røstvold Thorsen, Visma Consulting.
 */
public class DecodeUtils {
	private static final String CHARSET = java.nio.charset.StandardCharsets.UTF_8.name();

	public static String[] decodeBasicAuth(String header) throws IOException {
		byte[] base64Token = header.substring(6).getBytes(CHARSET);
		byte[] decoded;

		try {
			decoded = Base64.getDecoder().decode(base64Token);
		} catch (IllegalArgumentException e) {
			throw new BadCredentialsException(
					"Kunne ikke dekode basic authentication token");
		}

		String token = new String(decoded, CHARSET);
		int delim = token.indexOf(':');

		if (delim == -1) {
			throw new BadCredentialsException("Ugyldig basic authentication token");
		}
		return new String[]{token.substring(0, delim), token.substring(delim + 1)};
	}

}
