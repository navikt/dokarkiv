package no.nav.dokarkiv.core.storage;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class RetryConstants {

	//retries: 2s, 10s, 50s, 4.2min, 20.8min, sum = max vente-tid = 26min. maxAttempts inkluderer første forsøk
	public static final int MAX_ATTEMPTS_LONG = 6;
	public static final int DELAY_LONG = 2000;
	public static final int MULTIPLIER_LONG = 5;

	//retries: 500ms, 1s sum = max vente-tid = 1.5s. maxAttempts inkluderer første forsøk
	public static final int MAX_ATTEMPTS_SHORT = 3;
	public static final int DELAY_SHORT = 500;
	public static final int MULTIPLIER_SHORT = 2;

}
