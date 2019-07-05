package no.nav.dokarkiv.core.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

/**
 * @author Olav Røstvold Thorsen, Visma Consulting.
 */
@Slf4j
public final class DecodeUtils {
	private static final String CHARSET = java.nio.charset.StandardCharsets.UTF_8.name();

	private DecodeUtils() {
	}

	public static String[] decodeBasicAuth(String header) {
		byte[] decoded;
		try {
			byte[] base64Token = header.substring(6).getBytes(CHARSET);
			decoded = Base64.getDecoder().decode(base64Token);
			String token = new String(decoded, CHARSET);
			int delim = token.indexOf(':');
			if (delim == -1) {
				throw new BadCredentialsException("Ugyldig basic authentication token");
			}
			return new String[]{token.substring(0, delim), token.substring(delim + 1)};
		} catch (IllegalArgumentException | UnsupportedEncodingException e) {
			log.error(e.getMessage(), e.getCause());
		}
		return new String[]{};
	}

}
