package no.nav.dokarkiv.core.util;

import java.util.regex.Pattern;

public class SafeLoggingUtil {
	private static final Pattern safeCharsOnly = Pattern.compile("[^a-zA-Z0-9]");

	public static String removeUnsafeChars(String input) {
		return safeCharsOnly.matcher(input).replaceAll("_");
	}
}
