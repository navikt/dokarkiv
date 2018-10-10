package no.nav.dokarkiv.core.util;

import com.amazonaws.util.json.Jackson;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class JsonSerializer {

	public static String serialize(Object object) {
		return Jackson.toJsonString(object);
	}

	public static <T> T deserialize(String jsonPayload, Class<T> tClass) {
		return Jackson.fromJsonString(jsonPayload, tClass);
	}
}
