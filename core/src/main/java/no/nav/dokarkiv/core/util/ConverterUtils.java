package no.nav.dokarkiv.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class ConverterUtils {

	public static <T extends Enum<T>> T stringToEnum(Class<T> clazz, String value) {
		if (value == null) {
			return null;
		}

		return Enum.valueOf(clazz, value);
	}

	public static <T> T jsonStringToObject(String hendelseInfoHeader, Class<T> tClass) throws IOException {
		ObjectMapper mapper = new ObjectMapper();

		return mapper.readValue(hendelseInfoHeader, tClass);

	}


	public static String objectToJsonString(Object object) throws IOException {
		ObjectMapper mapper = new ObjectMapper();

		return mapper.writeValueAsString(object);

	}
}
