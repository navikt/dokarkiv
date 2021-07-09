package no.nav.dokarkiv.core.security.handler;

import java.util.regex.Pattern;

/**
 * Konstanter for token handlers.
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
final class HandlerConstants {
	static final String NAVIDENT_REGEX = "^[a-zA-Z]\\d{6}$";
	static final Pattern NAVIDENT_PATTERN = Pattern.compile(NAVIDENT_REGEX);

	private HandlerConstants() {
		// ingen instansiering
	}
}
