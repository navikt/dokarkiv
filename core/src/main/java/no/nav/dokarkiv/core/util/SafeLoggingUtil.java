package no.nav.dokarkiv.core.util;

import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.abbreviate;

public class SafeLoggingUtil {
	private static final int STRING_MAXLENGTH = 500;
	private static final Pattern EVERYTHING_EXCEPT_SAFE_CHARS_REGEX = Pattern.compile("[^a-zA-Z0-9]");

	public static String removeUnsafeChars(String input) {
		if (input == null) {
			return null;
		}
		return abbreviate(EVERYTHING_EXCEPT_SAFE_CHARS_REGEX.matcher(input).replaceAll("_"), STRING_MAXLENGTH);
	}
}
