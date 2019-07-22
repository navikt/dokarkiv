package no.nav.dokarkiv.core;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public final class NavHeaders {
	public static final String NAV_CALL_ID = "Nav-Callid";
	public static final String NAV_CONSUMER_ID = "Nav-Consumer-Id";
	@Deprecated // Konsumenter skal bruke NAV_CALL_ID i stedet.
	public static final String X_CORRELATION_ID = "X-Correlation-ID";

	private NavHeaders() {
		//noop
	}
}
