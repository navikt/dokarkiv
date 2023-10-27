package no.nav.dokarkiv.core.storage;

public class RetryConstants {
	//retries: 500ms, 1s sum = max vente-tid = 1.5s. maxAttempts inkluderer første forsøk
	public static final int MAX_ATTEMPTS_SHORT = 3;
	public static final int DELAY_SHORT = 500;
	public static final int MULTIPLIER_SHORT = 2;
}
