package no.nav.dokarkiv.core.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class JsonSerializer {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	static {
		objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	public static <T> T deserialize(String jsonPayload, Class<T> tClass) {
		try {
			return objectMapper.readValue(jsonPayload, tClass);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException(e);
		}
	}
}
