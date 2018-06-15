package no.nav.dokarkiv.core.domain.util;

import no.stelvio.common.util.DateUtil;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * DateProvider is used to get todays date. The date can be mocked out for
 * testing purpose.
 * <p>
 * DatePattern is DB2_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss".
 *
 * @author Ole Hjalmar Herje, BEKK
 * @author Magnus Skuland, Sirius IT
 */
public class DateProvider {

	/**
	 * ISO 639 language code
	 */
	private static final String NORWAY = "nb";
	/**
	 * The provider to use.
	 */
	private static Provider provider;
	private boolean mockMode;
	private String mockDate;

	/**
	 * Sets date to return as todays date if {@link #mockMode} is true.
	 * DatePattern is DB2_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss".
	 *
	 * @param mockDate the mockDate to set
	 */
	public void setMockDate(String mockDate) {
		this.mockDate = mockDate;
	}

	/**
	 * Set to true to return configured mockdate.
	 *
	 * @param mockMode the mockMode to set
	 */
	public void setMockMode(boolean mockMode) {
		this.mockMode = mockMode;
	}

	/**
	 * Configures a DateProvider. Can be called from unit test classes.
	 *
	 * @param mockMode see {@link #setMockMode(boolean)}.
	 * @param mockDate see {@link #setMockDate(String)}.
	 */
	public static void configure(final boolean mockMode, final String mockDate) {
		if (mockMode) {
			provider = getMockProvider(mockDate);
		} else {
			provider = getNormalProvider();
		}
	}

	/**
	 * Spring init-method. Initializes dateprovider in configured mode.
	 */
	public void initialize() {
		if (mockMode) {
			provider = getMockProvider(mockDate);
		} else {
			provider = getNormalProvider();
		}
	}

	/**
	 * Gets todays date. Real date by default, or mocked date if configured.
	 *
	 * @return Todays date.
	 */
	public static Date getToday() {
		if (provider == null) {
			provider = getNormalProvider();
		}
		return provider.getToday();
	}

	/**
	 * Gets a string representation of date in the format used by this
	 * DateProvider.
	 *
	 * @param date Date to format
	 * @return A formatted date, see {@value #DATE_PATTERN}.
	 */
	public static String getDate(Date date) {
		return DateUtil.formatDB2String(date);
	}

	/**
	 * Gets a provider that gives actual date from calendar.
	 *
	 * @return A normal date provider.
	 */
	private static Provider getNormalProvider() {
		return new Provider() {
			public Date getToday() {
				return new GregorianCalendar(new Locale(NORWAY)).getTime();
			}
		};
	}

	/**
	 * Gets a provider that gives a configured date.
	 *
	 * @param mockDate Date to return as today, must be in format:
	 * @return A mock date provider.
	 */
	private static Provider getMockProvider(final String mockDate) {
		return new Provider() {
			public Date getToday() {
				return DateUtil.parseDB2String(mockDate, false);
			}
		};
	}

	/**
	 * Defines a date provider interface.
	 *
	 * @author Ole Hjalmar Herje, BEKK
	 */
	interface Provider {
		/**
		 * Get todays date.
		 *
		 * @return today's date
		 */
		Date getToday();
	}

}
