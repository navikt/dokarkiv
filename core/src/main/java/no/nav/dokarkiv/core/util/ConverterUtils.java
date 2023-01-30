package no.nav.dokarkiv.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.security.token.support.core.jwt.JwtToken;

import java.io.IOException;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class ConverterUtils {

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

	public static <T> T jsonStringToObject(String jsonString, Class<T> tClass) throws IOException {
		ObjectMapper mapper = new ObjectMapper();

		return mapper.readValue(jsonString, tClass);

	}


	public static <T> List<T> jsonStringToObjectList(String jsonString, Class<T> tClass) throws IOException {
		ObjectMapper mapper = new ObjectMapper();

		return mapper.readValue(jsonString, mapper.getTypeFactory().constructCollectionType(List.class, tClass));

	}


	public static String objectToJsonString(Object object) throws IOException {
		ObjectMapper mapper = new ObjectMapper();

		return mapper.writeValueAsString(object);

	}

	public static String getSubJwtTokenClaim(String accessToken) {
		return isBlank(accessToken) ? null : new JwtToken(accessToken).getJwtTokenClaims().getStringClaim(DEFAULT_CLAIM_SUB);
	}
}
