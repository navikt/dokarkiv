package no.nav.dokarkiv.core.domain.util;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * DateProvider is used to get todays date. The date can be mocked out for
 * testing purpose.
 * <p>
 * DatePattern is DB2_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss".
 *
 * @deprecated Hvis man behøver spesifikke tidspunkt mtp test. Vurder å supplere en Clock i implementasjonen.
 *
 * @author Ole Hjalmar Herje, BEKK
 * @author Magnus Skuland, Sirius IT
 */
@Deprecated
public class DateProvider {

	/**
	 * ISO 639 language code
	 */
	private static final String NORWAY = "nb";
	/**
	 * The provider to use.
	 */
	private static Provider provider;

	/**
	 * Configures a DateProvider. Can be called from unit test classes.
	 */
	public static void configure(final boolean mockMode, final String mockDate) {
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
	 * @return A formatted date.
	 */
	public static String getDate(Date date) {
		return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).toString();
	}

	/**
	 * Gets a provider that gives actual date from calendar.
	 *
	 * @return A normal date provider.
	 */
	private static Provider getNormalProvider() {
		return () -> new GregorianCalendar(new Locale(NORWAY)).getTime();
	}

	/**
	 * Gets a provider that gives a configured date.
	 *
	 * @param mockDate Date to return as today, must be in format:
	 * @return A mock date provider.
	 */
	private static Provider getMockProvider(final String mockDate) {
		return () -> Date.from(LocalDateTime.parse(mockDate ).atZone(ZoneId.systemDefault()).toInstant());
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
