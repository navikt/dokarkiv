package no.nav.dokarkiv.core.util;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.security.token.support.core.jwt.JwtToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

	public static <T> T jsonStringToObject(String jsonString, Class<T> tClass) throws IOException {
		return mapper.readValue(jsonString, tClass);
	}


	public static <T> List<T> jsonStringToObjectList(String jsonString, Class<T> tClass) throws IOException {
		return mapper.readValue(jsonString, mapper.getTypeFactory().constructCollectionType(List.class, tClass));
	}

	public static <T> List<Map<String, T>> jsonStringToListOfKeyValueMap(String jsonString, Class<T> tClass) throws IOException {
		JavaType mapType = mapper.getTypeFactory().constructMapType(Map.class, String.class, tClass);
		return mapper.readValue(jsonString, mapper.getTypeFactory().constructCollectionLikeType(ArrayList.class, mapType));
	}


	public static String objectToJsonString(Object object) throws IOException {
		return mapper.writeValueAsString(object);
	}

	public static String getSubJwtTokenClaim(String accessToken) {
		return isBlank(accessToken) ? null : new JwtToken(accessToken).getJwtTokenClaims().getStringClaim(DEFAULT_CLAIM_SUB);
	}
}