package no.nav.dokarkiv.core.util;

import org.springframework.security.authentication.BadCredentialsException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;

/**
 * @author Olav Røstvold Thorsen, Visma Consulting.
 */
public final class DecodeUtils {
	private static final String CHARSET = java.nio.charset.StandardCharsets.UTF_8.name();

	private DecodeUtils(){
	}

	public static String[] decodeBasicAuth(String header)  {
		byte[] base64Token = new byte[0];
		try {
			base64Token = header.substring(6).getBytes(CHARSET);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		byte[] decoded;

		try {
			decoded = Base64.getDecoder().decode(base64Token);
		} catch (IllegalArgumentException e) {
			throw new BadCredentialsException(
					"Kunne ikke dekode basic authentication token");
		}

		String token = null;
		try {
			token = new String(decoded, CHARSET);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		int delim = token.indexOf(':');

		if (delim == -1) {
			throw new BadCredentialsException("Ugyldig basic authentication token");
		}
		return new String[]{token.substring(0, delim), token.substring(delim + 1)};
	}

}
