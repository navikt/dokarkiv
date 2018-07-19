package no.nav.dokarkiv.core.utils;

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

}
