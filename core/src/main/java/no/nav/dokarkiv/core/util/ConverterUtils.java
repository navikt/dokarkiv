package no.nav.dokarkiv.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

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
}
