package no.nav.dokarkiv.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.security.token.support.core.jwt.JwtToken;

import java.io.IOException;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class ConverterUtils {

	private static final ObjectMapper mapper = new ObjectMapper();
	static final String DEFAULT_CLAIM_SUB = "sub";

	public static <T extends Enum<T>> T stringToEnum(Class<T> clazz, String value) {
		if (value == null) {
			return null;
		}

		return Enum.valueOf(clazz, value);
	}

	public static String enumToString(Enum en) {
		return en == null ? null : en.name();
	}

	public static String objectToJsonString(Object object) throws IOException {
		return mapper.writeValueAsString(object);
	}

	public static String getSubJwtTokenClaim(String accessToken) {
		return isBlank(accessToken) ? null : new JwtToken(accessToken).getJwtTokenClaims().getStringClaim(DEFAULT_CLAIM_SUB);
	}
}