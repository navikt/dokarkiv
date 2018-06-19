package no.nav.dokarkiv.core.stelvio;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.Period;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Utility for localized parsing and formatting of dates.
 *
 * @author Petter Skodvin, Accenture
 * @author Jarle Rudihagen, Accenture
 * @author Rune R�ren, Accenture
 * @author Eivind Staff, Accenture
 * @version $Id: DateUtil.java 2833 2006-03-10 09:50:32Z skb2930 $
 */
public final class DateUtil {

	/**
	 * Used for testing the earlist year accepted for parsing.
	 */
	private static final int EARLIEST_YEAR = 1000;

	/**
	 * Used for calculating months between 2 dates.
	 */
	private static final int MONTHS_IN_YEAR = 12;

	/* Legal date formats. */
	private static final String DATE_FORMAT = "dd.MM.yyyy";

	private static final String COMPACT_DATE_FORMAT = "ddMMyyyy";

	private static final String SHORT_DATE_FORMAT = "dd.MM.yy";

	private static final String DB2_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	private static final String CICS_DATE_FORMAT = "yyyyMMdd";

	private static final String TSS_DATE_FORMAT = "yyyyMMdd";

	private static final String TSS_TID_REG_FORMAT = "yyyyMMddHHmm";

	private static final String OPPDRAG_DATE_FORMAT = "yyyy-MM-dd";

	private static final String TPS_DATE_FORMAT = "yyyy-MM-dd";

	private static final String BATCH_MONTH_FORMAT = "yyyyMM";

	private static final String TPS_BORN_DATE_FORMAT = "ddMMyy";

	private static final String MONTHLY_PERIOD_FORMAT = "MM.yyyy";

	private static final String TIMESTAMP_PERIOD_FORMAT = "dd.MM.yyyy HH:mm:ss:SS";

	private static final String TIME_OF_DAY_FORMAT = "HHmmss";

	private static DateCreator dateCreator = new DefaultDateCreator();

	/**
	 * Representation of ETERNITY (31.12.9999).
	 */
	public static final Date ETERNITY = java.sql.Date.valueOf("9999-12-31");

	/**
	 * Should not be instantiated.
	 */
	private DateUtil() {
	}

	/**
	 * Parse a string in following formats.
	 * <p>
	 * <ul>
	 * <li>ddMMyy
	 * <li>dd.MM.yy
	 * <li>dd.MM.yyyy
	 * <li>dd/MM/yy
	 * <li>dd/MM/yyyy
	 * <li>dd\MM\yy
	 * <li>dd\MM\yyyy
	 * <li>dd-MM-yy
	 * <li>dd-MM-yyyy
	 * </ul>
	 *
	 * @param input the String to parse
	 * @param allowNull true to allow empty String
	 * @return a Date or null if input is null and it is allowed.
	 * @throws IllegalArgumentException if input is not legal.
	 */
	public static Date parseInputString(final String input, final boolean allowNull) throws IllegalArgumentException {
		if (StringUtils.isBlank(input)) {
			if (allowNull) {
				return null;
			} else {
				throw createNullDateIsIllegalArgumentException();
			}
		}

		if (8 == input.length()) {
			return parseCommon(replaceSeparators(input), allowNull, SHORT_DATE_FORMAT, true, false, false);
		} else if (10 == input.length()) {
			return parseCommon(replaceSeparators(input), allowNull, DATE_FORMAT, true, false, false);
		} else if (6 == input.length()) {
			return parseCommon(input, allowNull, TPS_BORN_DATE_FORMAT, true, false, false);
		} else {
			throw new IllegalArgumentException("Failed to parse <" + input + ">. Not a valid input date");
		}
	}

	/**
	 * Parse a String into a Date.
	 *
	 * @param input the String to parse of the format ddMMyyyy.
	 * @return a Date, null if input is null or an empty String
	 * @throws IllegalArgumentException if input is not legal.
	 */
	public static Date parseCompactDate(final String input) throws IllegalArgumentException {
		return parseCommon(input, false, COMPACT_DATE_FORMAT, true, false, false);
	}

	/**
	 * Parse a String into a Date.
	 *
	 * @param input the String to parse
	 * @return a Date, null if input is null or an empty String
	 * @throws IllegalArgumentException if input is not legal.
	 */
	public static Date parse(final String input) throws IllegalArgumentException {
		return parse(input, true);
	}

	/**
	 * Parse a String into a Date.
	 *
	 * @param input the String to parse
	 * @param allowNull true to allow empty String
	 * @return a Date, null if input is null or an empty String
	 * @throws IllegalArgumentException if input is not legal.
	 */
	public static Date parse(final String input, final boolean allowNull) throws IllegalArgumentException {
		return parseCommon(input, allowNull, DATE_FORMAT, true, true, false);
	}

	/**
	 * Parser dato angitt som string fra TPS (formatet er yyyy-MM-dd).
	 *
	 * @param input dato som en streng
	 * @return Date objekt som er parset fra strengen
	 * @throws IllegalArgumentException if input is not legal.
	 */
	public static Date parseTpsDate(final String input) throws IllegalArgumentException {
		return parseCommon(input, false, TPS_DATE_FORMAT, false, true, false);
	}

	/**
	 * Parse a CICS date into a Java Date.
	 *
	 * @param input the String to parse.
	 * @return a Date, null if input is null or an empty String.
	 * @throws IllegalArgumentException if input is not legal.
	 */
	public static Date parseCicsDate(final String input) throws IllegalArgumentException {
		return parseCommon(input, false, CICS_DATE_FORMAT, true, false, false);
	}

	/**
	 * Parse a TSS date into a Java Date.
	 *
	 * @param input the String to parse.
	 * @return a Date, null if input is null or an empty String.
	 * @throws IllegalArgumentException if input is not legal.
	 */
	public static Date parseTSSDate(final String input) throws IllegalArgumentException {
		return parseCommon(input, true, TSS_DATE_FORMAT, true, false, false);
	}

	/**
	 * Parse a TSS tid_reg into a Java Date.
	 *
	 * @param input the String to parse.
	 * @return a Date, null if input is null or an empty String.
	 * @throws IllegalArgumentException if input is not legal.
	 */
	public static Date parseTSSTidReg(final String input) throws IllegalArgumentException {
		return parseCommon(input, true, TSS_TID_REG_FORMAT, true, false, false);
	}

	/**
	 * Parse a String into a Date with batch month pattern (yyyyMM).
	 *
	 * @param input the String to parse
	 * @param allowNull true to allow empty String
	 * @return a Date, null if input is null or an empty String
	 * @throws IllegalArgumentException if input is not legal.
	 */
	public static Date parseBatchMonth(final String input, final boolean allowNull) throws IllegalArgumentException {
		return parseCommon(input, allowNull, BATCH_MONTH_FORMAT, true, true, true);
	}

	/**
	 * Parse a string in format yyyy-MM-dd HH:mm:ss into a Date.
	 *
	 * @param input the String to parse
	 * @param allowNull true to allow empty String
	 * @return a Date, null if input is null or an empty String
	 * @throws IllegalArgumentException if input is not legal.
	 */
	public static Date parseDB2String(final String input, final boolean allowNull) throws IllegalArgumentException {
		return parseCommon(input, allowNull, DB2_DATE_FORMAT, true, false, false);
	}

	/**
	 * Check if the date is last day of month.
	 *
	 * @param date the date to check.
	 * @return true if date is last day of month.
	 */
	public static boolean isLastDayOfMonth(Date date) {
		if (null == date) {
			throw createNullDateIsIllegalArgumentException();
		}

		Calendar calendar = createCalendar(date);

		final int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
		final int lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

		return dayOfMonth == lastDayOfMonth;
	}

	/**
	 * Format a Date into a localized String (dd.MM.yyyy).
	 *
	 * @param input the date to format.
	 * @return the formatted date or empty string for null input or "eternity" (31.12.9999).
	 * @see #ETERNITY
	 */
	public static String format(final Date input) {
		if (null == input || ETERNITY.equals(input)) {
			return "";
		} else {
			return createDateFormat(DATE_FORMAT).format(input);
		}
	}

	/**
	 * Formats a Date into a timestamp period format (dd.MM.yyyy HH:mm:ss:SS).
	 *
	 * @param input the date to format.
	 * @return the formatted date.
	 */
	public static String formatTimestamp(final Date input) {
		if (null == input) {
			return "";
		} else {
			return createDateFormat(TIMESTAMP_PERIOD_FORMAT).format(input);
		}
	}

	/**
	 * Formats a Date into a monthly period format (MM.yyyy).
	 *
	 * @param input the date to format.
	 * @return the formatted date.
	 */
	public static String formatMonthlyPeriod(final Date input) {
		if (null == input) {
			return "";
		} else {
			return createDateFormat(MONTHLY_PERIOD_FORMAT).format(input);
		}
	}

	/**
	 * Formats a Date into DB2 format (yyyy-MM-dd HH:mm:ss).
	 *
	 * @param input the date to format.
	 * @return the formatted date.
	 */
	public static String formatDB2String(final Date input) {
		return createDateFormat(DB2_DATE_FORMAT).format(input);
	}

	/**
	 * Formats a Date into CICS format (yyyyMMdd).
	 *
	 * @param input the date to format.
	 * @return the formatted date.
	 */
	public static String formatCICSString(final Date input) {
		return createDateFormat(CICS_DATE_FORMAT).format(input);
	}

	/**
	 * Formats a Date into TSS format (yyyyMMdd).
	 *
	 * @param input the date to format.
	 * @return the formatted date.
	 */
	public static String formatTSSString(final Date input) {
		if (null == input) {
			return "";
		} else {
			return createDateFormat(TSS_DATE_FORMAT).format(input);
		}
	}

	/**
	 * Formats a Date into time of day format (MMmmss).
	 *
	 * @param input the date to format.
	 * @return the formatted date.
	 */
	public static String formatTimeOfDay(final Date input) {
		if (null == input) {
			return "";
		} else {
			return createDateFormat(TIME_OF_DAY_FORMAT).format(input);
		}
	}

	/**
	 * Formats a Date into TSS format (yyyyMMdd).
	 *
	 * @param input the date to format.
	 * @return the formatted date.
	 */
	public static String formatTSSTidRegString(final Date input) {
		return createDateFormat(TSS_TID_REG_FORMAT).format(input);
	}

	/**
	 * Formats a Date into Oppdrag format (yyyy-MM-dd).
	 *
	 * @param input the date to format.
	 * @return the formatted date.
	 */
	public static String formatOppdragString(final Date input) {
		return createDateFormat(OPPDRAG_DATE_FORMAT).format(input);
	}

	/**
	 * Formats a Date into batch month format (yyyyMM).
	 *
	 * @param input the date to format.
	 * @return the formatted date.
	 */
	public static String formatBatchMonthString(final Date input) {
		return createDateFormat(BATCH_MONTH_FORMAT).format(input);
	}

	/**
	 * Formats a Data into TPS born date format (ddMMyy).
	 *
	 * @param input the date to format.
	 * @return the formatted date.
	 */
	public static String formatTpsBornDateString(final Date input) {
		return createDateFormat(TPS_BORN_DATE_FORMAT).format(input);
	}

	/**
	 * Formats a input String to date format dd.mm.yyyy.
	 *
	 * @param input the date to format.
	 * @return the formatted date.
	 */
	public static String formatInputDateString(final String input) {
		Date date = parseInputString(input, true);

		if (null == date) {
			return "";
		} else {
			return createDateFormat(DATE_FORMAT).format(date);
		}
	}

	/**
	 * Checks if the day of the specified date is today's day.
	 *
	 * @param date the date to check.
	 * @return true if the specified date's day is today's day, false otherwise.
	 */
	public static boolean isToday(final Date date) {
		return isSameDay(date, dateCreator.createDate());
	}

	/**
	 * Checks if the day of the specified date is after today's day.
	 *
	 * @param date the date to check.
	 * @return true if specified date is after today's date, false otherwise.
	 */
	public static boolean isAfterToday(final Date date) {
		if (null == date) {
			throw createNullDateIsIllegalArgumentException();
		}

		return !isBeforeToday(date) && !isToday(date);
	}

	/**
	 * Checks if the day of the specified date is before today's date.
	 *
	 * @param date the date to check.
	 * @return true if specified date is before today's date, false otherwise.
	 */
	public static boolean isBeforeToday(final Date date) {
		return isBeforeDay(date, null);
	}

	/**
	 * Checks if one date is before another date. Only uses the date portion of the input, not taking the time portion in
	 * account.
	 *
	 * @param firstDate the first date.
	 * @param secondDate the second date to check against.
	 * @return true if first date is before the second date, false otherwise.
	 */
	public static boolean isBeforeDay(final Date firstDate, final Date secondDate) {
		if (null == firstDate) {
			return false;
		}

		final Calendar firstCalendar = createEmptyTimeFieldsCalendar(firstDate);
		// Use todays date if second date is empty
		final Calendar secondCalendar = createEmptyTimeFieldsCalendar(null == secondDate ? dateCreator.createDate()
				: secondDate);

		return firstCalendar.getTime().before(secondCalendar.getTime());
	}

	/**
	 * Method that checks if a date is between two other dates.
	 *
	 * @param compDate date that is being checked.
	 * @param fomDate FOM-date.
	 * @param tomDate TOM-date.
	 * @return boolean if date is in period.
	 */
	public static boolean isDateInPeriod(final Date compDate, final Date fomDate, final Date tomDate) {
		if (null == fomDate || null == compDate) {
			return false;
		}

		boolean tomOK = false;

		if (null != tomDate) {
			if (isBeforeDay(compDate, tomDate) || isSameDay(compDate, tomDate)) {
				tomOK = true;
			}
		} else {
			tomOK = true;
		}

		return (isBeforeDay(fomDate, compDate) || isSameDay(compDate, fomDate)) && tomOK;
	}

	/**
	 * Checks whether the date is the first day of a month or not.
	 *
	 * @param date the date to check.
	 * @return true if the date is the first day of a month, false otherwise.
	 */
	public static boolean isFirstDayOfMonth(Date date) {
		if (null == date) {
			throw createNullDateIsIllegalArgumentException();
		}

		final Calendar calendar = createCalendar(date);

		return calendar.getActualMinimum(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * Returns yesterday's date formatted for display.
	 *
	 * @return yesterday.
	 */
	public static Date getYesterday() {
		return getRelativeDateFromNow(-1);
	}

	/**
	 * Returns the date n days from now.
	 *
	 * @param days number of days relative from today (e.g. -14 = 2 weeks ago, 7 = in a week)
	 * @return the date.
	 */
	public static Date getRelativeDateFromNow(int days) {
		return getRelativeDateByDays(dateCreator.createDate(), days);
	}

	/**
	 * Creates a sorted list of <code>Date</code>s with the newest first.
	 *
	 * @param dates a set with the <code>Date</code>s to sort.
	 * @return a sorted list of <code>Date</code>s with the newest first.
	 */
	public static List<Date> sortDatesNewestFirst(Set<Date> dates) {
		if (null == dates) {
			throw createNullDateIsIllegalArgumentException();
		}

		List<Date> sortedDates = new ArrayList<Date>(dates);
		Collections.sort(sortedDates, Collections.reverseOrder()); // The newest date becomes the first

		return sortedDates;
	}

	/**
	 * Finds the last day in the month before the given date.
	 *
	 * @param date the date to work against.
	 * @return the last day in the month before the given date.
	 */
	public static Date findLastDayInMonthBefore(final Date date) {
		final Calendar calendar = createCalendar(date);

		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
		calendar.add(Calendar.DAY_OF_MONTH, -1);

		return calendar.getTime();
	}

	/**
	 * Returns the first day of month for the given date.
	 *
	 * @param date a given date
	 * @return the first day of the month for the given date.
	 */
	public static Date getFirstDayOfMonth(Date date) {
		return getFirstOrLastDayOfMonth(date, true);
	}

	/**
	 * Returns the last day of month for the given date.
	 *
	 * @param date a given date
	 * @return the last day of the month for the given date.
	 */
	public static Date getLastDayOfMonth(Date date) {
		return getFirstOrLastDayOfMonth(date, false);
	}

	/**
	 * Metode for � finne datoer X �r frem/tilbake i tid.
	 *
	 * @param date Dato
	 * @param years �r fremover (positive tall) eller �r bakover (negative tall)
	 * @return Datoen X �r frem eller tilbake
	 */
	public static Date getRelativeDateByYear(Date date, int years) {
		final Calendar calendar = createCalendar(date);
		calendar.add(Calendar.YEAR, years);

		return calendar.getTime();
	}

	/**
	 * Metode for � finne datoer X m�neder fremover/tilbake i tid.
	 *
	 * @param date Datoen
	 * @param months M�ender fremover / bakover
	 * @return Datoen X m�neder fremover/tilbake
	 */
	public static Date getRelativeDateByMonth(Date date, int months) {
		final Calendar calendar = createCalendar(date);
		calendar.add(Calendar.MONTH, months);

		return calendar.getTime();
	}

	/**
	 * Metode for � finne datoer X dager frem / tilbake i tid.
	 *
	 * @param date Datoe
	 * @param days dager frem/tilbake
	 * @return Datoen X dager fremover/tilbake
	 */
	public static Date getRelativeDateByDays(Date date, int days) {
		final Calendar calendar = createCalendar(date);
		calendar.add(Calendar.DAY_OF_MONTH, days);

		return calendar.getTime();
	}

	/**
	 * Setter klokkeslettet til 00:00:00.00.
	 *
	 * @param inDate Aktuell dato
	 * @return nullstilt dato.
	 */
	public static Date setTimeToZero(Date inDate) {
		return createEmptyTimeFieldsCalendar(inDate).getTime();
	}

	/**
	 * Returnerer �ret i en dato.
	 *
	 * @param inDate angitt dato.
	 * @return �ret i datoen.
	 */
	public static int getYear(Date inDate) {
		return getField(inDate, Calendar.YEAR);
	}

	/**
	 * Returnerer m�ned i en dato.
	 *
	 * @param inDate angitt dato.
	 * @return m�ned i datoen.
	 */
	public static int getMonth(Date inDate) {
		return getField(inDate, Calendar.MONTH);
	}

	/**
	 * Metode som finner f�rste dagen i �ret for angitt dato. Timer, minutter, sekunder og millisekunder er nullstilt.
	 *
	 * @param inDate hvilken dato som skal benyttes
	 * @return f�rste dag
	 */
	public static Date getFirstDateInYear(Date inDate) {
		final Calendar calendar = createEmptyTimeFieldsCalendar(inDate);

		calendar.set(Calendar.MONTH, Calendar.JANUARY);
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));

		return calendar.getTime();
	}

	/**
	 * Metode som finner siste dagen i �ret for angitt dato Timer, minutter, sekunder og millisekunder er nullstilt.
	 *
	 * @param inDate hvilken dato som skal benyttes
	 * @return siste dag
	 */
	public static Date getLastDateInYear(Date inDate) {
		final Calendar calendar = createEmptyTimeFieldsCalendar(inDate);

		calendar.set(Calendar.MONTH, Calendar.DECEMBER);
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));

		return calendar.getTime();
	}

	/**
	 * Returns the number of month between two dates.
	 *
	 * @param fromDate date from.
	 * @param toDate date to.
	 * @return relative number of months between fromDate and toDate.
	 */
	public static int getMonthBetween(Date fromDate, Date toDate) {
		final Calendar fromCalendar = createCalendar(fromDate);
		final Calendar toCalendar = createCalendar(toDate);

		int fromTotalMonths = MONTHS_IN_YEAR * fromCalendar.get(Calendar.YEAR) + fromCalendar.get(Calendar.MONTH);
		int toTotalMonths = MONTHS_IN_YEAR * toCalendar.get(Calendar.YEAR) + toCalendar.get(Calendar.MONTH);

		return Math.abs(fromTotalMonths - toTotalMonths);
	}

	/**
	 * Henter datoen 18 ar tilbake fra angitt dato. Feks hvis parameter dato er 15/5-2005 vil retur dato vare 15/5-1987.
	 *
	 * @param date den nye datoen
	 * @return Date 18 �r tilbake fra date eller null hvis date er null
	 */
	public static Date get18YearsBack(Date date) {
		if (null == date) {
			return null;
		}

		final Calendar calendar = createEmptyTimeFieldsCalendar(date);
		calendar.roll(Calendar.YEAR, -18);

		return calendar.getTime();
	}

	/**
	 * Creates a non-lenient <code>DateFormat</code> from the specified date format.
	 *
	 * @param dateFormat the date format the created <code>DateForm</code> will have.
	 * @return a non-lenient <code>DateFormat</code> with the specified date format.
	 */
	public static DateFormat createDateFormat(final String dateFormat) {
		SimpleDateFormat format = (SimpleDateFormat) DateFormat.getDateInstance();
		format.setLenient(false);
		format.applyLocalizedPattern(dateFormat);

		return format;
	}

	/**
	 * Helper method for parsing dates in a string.
	 *
	 * @param input the input string to parse into a date.
	 * @param allowNull whether null is allowed as input for the date.
	 * @param dateFormat the format to use for parsing the date string.
	 * @param noEarlyDates whether early dates should be allowed.
	 * @param resetTimePart whether the time part of the date should be reset to all zeros.
	 * @param resetDayOfMonth whether the day of month of the date should be reset to the first day of the month.
	 * @return the parsed date.
	 * @throws IllegalArgumentException if input is not legal.
	 */
	private static Date parseCommon(final String input, final boolean allowNull, String dateFormat, boolean noEarlyDates,
									boolean resetTimePart, boolean resetDayOfMonth) throws IllegalArgumentException {
		Date output = null;

		if (StringUtils.isBlank(input)) {
			if (!allowNull) {
				throw createNullDateIsIllegalArgumentException();
			}
		} else {
			try {
				output = createDateFormat(dateFormat).parse(input);
			} catch (ParseException pe) {
				throw new IllegalArgumentException("Failed to parse " + input + ": " + pe);
			}

			if (null != output) {
				final Calendar calendar = createCalendar(output);

				if (noEarlyDates) {
					// We don't accept years earlier than 1000
					if (EARLIEST_YEAR > calendar.get(Calendar.YEAR)) {
						throw new IllegalArgumentException("Don't accept years earlier than " + EARLIEST_YEAR + ": " + input);
					}
				}

				if (resetTimePart) {
					clearTimeFields(calendar);
				}

				if (resetDayOfMonth) {
					calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
				}

				output = calendar.getTime();
			}
		}

		return output;
	}

	/**
	 * Helper method for replacing not formattable separators with formattable separators.
	 *
	 * @param input the string to replace separators in.
	 * @return string with separators replaced.
	 */
	private static String replaceSeparators(final String input) {
		return StringUtils.replaceChars(input, "-/\\", "...");
	}

	/**
	 * Creates a non-lenient calendar with empty time fields.
	 *
	 * @param date the <code>Date</code> to create a calendar from.
	 * @return a non-lenient calendar with empty time fields.
	 * @see #createCalendar(Date)
	 */
	private static Calendar createEmptyTimeFieldsCalendar(final Date date) {
		final Calendar calendar = createCalendar(date);
		clearTimeFields(calendar);

		return calendar;
	}

	/**
	 * Creates a non-lenient calendar.
	 *
	 * @param date the <code>Date</code> to create a calendar from.
	 * @return a non-lenient calendar.
	 * @see #createCalendar(Date)
	 */
	private static Calendar createCalendar(final Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setLenient(false);
		calendar.setTime(date);

		return calendar;
	}

	/**
	 * Clears the time fields of a calendar.
	 *
	 * @param calendar the calendar to clear the time fields of.
	 */
	private static void clearTimeFields(final Calendar calendar) {
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
	}

	/**
	 * Returns the given field for a date. Be careful when calling this from inside another synchronized block as it could lead
	 * to a dealock.
	 *
	 * @param inDate the date to return the given field for.
	 * @param dateField hvilket felt som skal finnes (vil v�re feks Calendar.YEAR)
	 * @return verdien p� feltet
	 */
	private static int getField(Date inDate, int dateField) {
		final Calendar calendar = createCalendar(inDate);
		return calendar.get(dateField);
	}

	/**
	 * Returns the first or last of month for a given date. Be careful when calling this from inside another synchronized block
	 * as it could lead to a dealock.
	 *
	 * @param date a given date
	 * @param first is true then first day in month
	 * @return Date for the first or last day of month
	 */
	private static Date getFirstOrLastDayOfMonth(Date date, boolean first) {
		final Calendar calendar = createCalendar(date);
		int dayOfMonth;

		if (first) {
			dayOfMonth = calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
		} else {
			dayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		}

		calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

		return calendar.getTime();
	}

	/**
	 * Sets the date creator.
	 *
	 * @param dateCreator a DateCreator object
	 */
	public static void setDateCreator(DateCreator dateCreator) {
		DateUtil.dateCreator = dateCreator;
	}

	/**
	 * Interface for creating dates.
	 *
	 * @author Thomas Finneid
	 * @author Stig Kleppe-J�rgensen
	 */
	public static interface DateCreator {

		/**
		 * Creates a new Date.
		 *
		 * @return a new date
		 */
		Date createDate();
	}

	/**
	 * Utility for creating dates.
	 *
	 * @author Thomas Finneid
	 * @author Stig Kleppe-J�rgensen
	 */
	public static class DefaultDateCreator implements DateCreator {

		/**
		 * Returns a new date.
		 *
		 * @return the new date
		 */
		public Date createDate() {
			return new Date();
		}
	}

	/**
	 * First of two date formats that is supported. This is the full ISO standard format, stating year, month, day 'T', hours,
	 * minutes and seconds
	 */
	public static final String DATEFORMAT_ISO_STANDARD_FULL = "yyyy-MM-dd'T'HH:mm:ss";

	/**
	 * The second of two date formats that is supported. This is the compact ISO standard format, only stating the year, month
	 * and day, disregarding hours, minutes and seconds.
	 */
	public static final String DATEFORMAT_ISO_STANDARD_COMPACT = "yyyy-MM-dd";

	/**
	 * Check whether a closed date range intersects another closed date range. Will throw a NullPointerException if any of the
	 * passed parameters are null. To check for intersection there are several cases to be covered:
	 * <p>
	 * <pre>
	 *       |-------|          (the first period, called period 1)
	 *       |-------|		  0 same period is an intersection
	 * |---|                  1 no intersection with period 1
	 *                 |----| 2 no intersection with period 1
	 *  |-------|             3 intersection, it ends before period 1 ends
	 *             |------|   4 intersection, it starts before period 1 ends
	 * |----------------|     5 intersection, starts before and ends after period 1
	 *
	 * |-----|				  6 Special case, ends when period 1 starts (by milliseconds)
	 *               |------| 7 Special case, begins when period 1 ends  (by milliseconds)
	 * </pre>
	 *
	 * @param o1Start The start date of the first period
	 * @param o1End The end date of the first period
	 * @param o2Start The start date of the second period
	 * @param o2End The end date of the second period
	 * @param considerContactAsIntersection If cases 6 and 7 should be regarded as a intersection, set this flag to true.
	 * @return true if the periods do intersect by , false otherwise
	 * @author Truls J�rgensen (Capgemini)
	 */
	public static boolean intersectsByMilliseconds(Date o1Start, Date o1End, Date o2Start, Date o2End,
												   boolean considerContactAsIntersection) {

		boolean isPoint = o1Start.equals(o1End) || o2Start.equals(o2End);

		// get the max of starts
		long start = Math.max(o1Start.getTime(), o2Start.getTime());
		// get the min of ends
		long end = Math.min(o1End.getTime(), o2End.getTime());

		if (considerContactAsIntersection || isPoint) {
			return (start <= end);
		} else {
			return (start < end);
		}
	}

	/**
	 * {@see DateUtil.intersects} Removes the values for HOUR_OF_DAY, MINUTES, SECONDS and MILLISECONDS before the compare such
	 * that same day is regarded as intersection if <code>considerContactByDayAsIntersection</code> is true.
	 *
	 * @param o1Start The start date of the first period
	 * @param o1End The end date of the first period
	 * @param o2Start The start date of the second period
	 * @param o2End The end date of the second period
	 * @param considerContactByDayAsIntersection If cases 6 and 7 should be regarded as a intersection, set this flag to true.
	 * @return true if the periods do intersect by day, false otherwise
	 */
	public static boolean intersects(Date o1Start, Date o1End, Date o2Start, Date o2End,
									 boolean considerContactByDayAsIntersection) {
		Date o1StartDay = createDayCalendar(o1Start).getTime();
		Date o1EndDay = createDayCalendar(o1End).getTime();
		Date o2StartDay = createDayCalendar(o2Start).getTime();
		Date o2EndDay = createDayCalendar(o2End).getTime();

		return intersectsByMilliseconds(o1StartDay, o1EndDay, o2StartDay, o2EndDay, considerContactByDayAsIntersection);
	}

	/**
	 * Check whether a closed date range intersects an open ended date range. By convention, an open ended range intersects a
	 * closed range if it starts before the closed range starts. The reason is that there is really no way of telling WHEN an
	 * open ended range ends - the contradiction is obvious. Therefore, the open ended range gets assigned a fictional end date,
	 * reasonably close to infinity.
	 *
	 * @param o1Start The start date of the first period
	 * @param o1End The end date of the first period
	 * @param o2Start The start date of the second period
	 * @param considerContactAsIntersection If cases 6 and 7 should be regarded as a intersection, set this flag to true.
	 * @return true if the periods do intersect, false otherwise
	 * @author Truls J�rgensen (Capgemini)
	 * @see DateUtil.intersects
	 */
	public static boolean intersectsWithOpenEnd(Date o1Start, Date o1End, Date o2Start, boolean considerContactAsIntersection) {
		return intersects(o1Start, o1End, o2Start, new Date(Long.MAX_VALUE), considerContactAsIntersection);
	}

	/**
	 * {@see DateUtil.intersects} Removes the values for HOUR_OF_DAY, MINUTES, SECONDS and MILLISECONDS before the compare such
	 * that same day is regarded as intersection if <code>considerContactByDayAsIntersection</code> is true. The endings of
	 * the respective periods can be NULL, if so these will be set to infinity.
	 *
	 * @param o1Start The start date of the first period
	 * @param o1End The end date of the first period; can be null, if so is interpreted as infinity
	 * @param o2Start The start date of the second period
	 * @param o2End The end date of the second period; can be null, if so is interpreted as infinity
	 * @param considerContactByDayAsIntersection If cases 6 and 7 should be regarded as a intersection, set this flag to true.
	 * @return true if the periods do intersect by day, false otherwise
	 */
	public static boolean intersectsWithPossiblyOpenEndings(Date o1Start, Date o1End, Date o2Start, Date o2End,
															boolean considerContactByDayAsIntersection) {

		return intersects(o1Start, (o1End == null) ? new Date(Long.MAX_VALUE) : o1End, o2Start,
				(o2End == null) ? new Date(Long.MAX_VALUE) : o2End, considerContactByDayAsIntersection);
	}

	/**
	 * Returns the duration of the intersecting period. Returns an array of int, with
	 * <ul>
	 * <li>[0] as number of days </li>
	 * <li>[1] as number of months </li>
	 * <li>[2] as number of years </li>
	 * </ul>
	 *
	 * @param o1Start The start date of the first period
	 * @param o1End The end date of the first period
	 * @param o2Start The start date of the second period
	 * @param o2End The end date of the second period
	 * @param considerContactByDayAsIntersection If cases 6 and 7 should be regarded as a intersection, set this flag to true. Typically, this should be set to
	 * true if the end date is a TOM-date, meaning that it is regarded as part of the period.
	 * @return duration as an array of {years, months, days}
	 * @author Jan-Erik Carlsen (Capgemini)
	 */
	public static int[] getIntersectionDuration(Date o1Start, Date o1End, Date o2Start, Date o2End,
												boolean considerContactByDayAsIntersection) {

		if (!intersects(o1Start, o1End, o2Start, o2End, considerContactByDayAsIntersection)) {
			return new int[]{0, 0, 0};
		}

		long start = Math.max(o1Start.getTime(), o2Start.getTime());
		long end = Math.min(o1End.getTime(), o2End.getTime());

		// Set granularity to days
		Date startDate = new Date(start);
		Date endDate = new Date(end);
		Calendar startCal = createDayCalendar(startDate);
		Calendar endCal = createDayCalendar(endDate);

		if (considerContactByDayAsIntersection) {
			endCal.add(Calendar.DAY_OF_MONTH, 1);
		}
		return formatPeriodToArray(new Period(startCal.getTimeInMillis(), endCal.getTimeInMillis()));
	}

	/**
	 * Finds the lowest date of two dates by day, down to the granularity of days (not milliseconds, which is the default
	 * behaviour in the standard API). If one date is null, the other will be returned. If both are null, null will be returned.
	 *
	 * @param first the first date
	 * @param second the second date
	 * @return the earliest of the two dates
	 */
	public static Date findEarliestDateByDay(Date first, Date second) {
		if (first == null) {
			return second;
		} else if (second == null) {
			return first;
		} else {
			return isBeforeByDay(first, second, true) ? first : second;
		}
	}

	/**
	 * Finds the latest of two dates by day, down to the granularity of days (not milliseconds, which is the default behaviour
	 * in the standard API). If one date is null, the other will be returned. If both are null, null will be returned.
	 *
	 * @param first the first date
	 * @param second the second date
	 * @return the latest of the two dates
	 */
	public static Date findLatestDateByDay(Date first, Date second) {
		if (first == null) {
			return second;
		} else if (second == null) {
			return first;
		} else {
			return isAfterByDay(first, second, true) ? first : second;
		}
	}

	/**
	 * Parses a string to a calendar on standard ISO format.
	 *
	 * @param dateISOString on the standard iso format yyyy-MM-dd'T'HH:mm:ss, i.e 1982-03-13T17:25:43 OR optionally, only yyyy-MM-dd (i.e
	 * 1982-03-13)
	 * @return just parsed date
	 * @throws ParseException is the parsing went wrong
	 */
	public static Date parseISOString(String dateISOString) throws ParseException {
		DateFormat formatter = (dateISOString.length() == DATEFORMAT_ISO_STANDARD_COMPACT.length()) ? new SimpleDateFormat(
				DATEFORMAT_ISO_STANDARD_COMPACT) : new SimpleDateFormat(DATEFORMAT_ISO_STANDARD_FULL);
		formatter.setLenient(false);
		return formatter.parse(dateISOString);

	}

	/**
	 * Small utility method for comparing two dates down to the granularity of days (not milliseconds, which is the default
	 * behaviour in the standard API). If any date argument is null, it gets assigned to year zero, reasonably far from our
	 * time.
	 *
	 * @param thisDate the first date to check
	 * @param thatDate the second date to check
	 * @param allowSameDay if allowSameDay is true, the method returns true if thisDate is equal to thatDate with respect to year, month
	 * and day. If set to false, the method returns false on this condition
	 * @return true if thisDate is after thatDay, false otherwise
	 */
	public static boolean isAfterByDay(Date thisDate, Date thatDate, boolean allowSameDay) {
		return compareDates(thisDate, thatDate, allowSameDay, true);
	}

	/**
	 * Small utility method for comparing two dates down to the granularity of days (not milliseconds, which is the default
	 * behaviour in the standard API). If any date argument is null, it gets assigned to year zero, reasonably far from our
	 * time.
	 *
	 * @param thisDate the first date to check.
	 * @param thatDate the second date to check
	 * @param allowSameDay if allowSameDay is true, the method returns true if thisDate is equal to thatDate with respect to year, month
	 * and day. If set to false, the method returns false on this condition
	 * @return true if thisDate is before thatDay, false otherwise
	 */
	public static boolean isBeforeByDay(Date thisDate, Date thatDate, boolean allowSameDay) {
		return compareDates(thisDate, thatDate, allowSameDay, false);
	}

	/**
	 * Small utility method for comparing two dates down to the granularity of days. Returns true if the dates are the same day.
	 * Hours, minutes, seconds and milliseconds are not regarded.
	 *
	 * @param thisDate the first date to check.
	 * @param thatDate the second date to check.
	 * @return true if the dates point to the same day.
	 */
	public static boolean isSameDay(Date thisDate, Date thatDate) {
		if (thisDate == null && thatDate == null) {
			return false;
		}

		Calendar thisCal = createDayCalendar(thisDate);
		Calendar thatCal = createDayCalendar(thatDate);

		return thisCal.equals(thatCal);
	}

	/**
	 * Gets Days, Months and Years from a period represented by a FOM-date and a TOM-date. If any of the dates are null, an
	 * empty period is returned
	 *
	 * @param fomDate the first date in the period
	 * @param tomDate the last date in the period
	 * @param includeTomDate boolean that states whether the TOM date should be regarded as within the period or not.
	 * @return the period as an int[] of {days, months, years}
	 */
	public static int[] getDaysMonthsYears(Date fomDate, Date tomDate, boolean includeTomDate) {
		if (tomDate == null || fomDate == null) {
			return new int[]{0, 0, 0};
		}
		Calendar fomCal = createDayCalendar(fomDate);
		Calendar tomCal = createDayCalendar(tomDate);

		if (includeTomDate) {
			tomCal.add(Calendar.DAY_OF_MONTH, 1);
		}
		Period period = new Period(fomCal.getTimeInMillis(), tomCal.getTimeInMillis());
		return formatPeriodToArray(period);
	}

	/**
	 * Creates a new Date object set to the date fields given by the input parameters. All other date fields are cleared.
	 *
	 * @param year the year to set
	 * @param month the month to set
	 * @param day the day of the month to set
	 * @return a new Date object
	 */
	public static Date createDate(int year, int month, int day) {
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(year, month, day);
		return cal.getTime();
	}

	/**
	 * Gets years and months from a period represented by a FOM date and a TOM date. If any of the dates are null, an empty
	 * period is returned. The days in the FOM and TOM dates are discarded. Example: FOM date 01.01.2011 and a TOM date of
	 * 10.03.2012 will return the following String: 1 �r, 2mnd.
	 *
	 * @param fomDate the first date in the period
	 * @param tomDate the last date in the period
	 * @param includeTomDate boolean that states whether the TOM date should be included in the period or not.
	 * @return the string describing the duration of the period in years and months (x �r, y mnd.)
	 */
	public static String getNumberOfYearsAndMonths(Date fomDate, Date tomDate, boolean includeTomDate) {
		StringBuilder stringBuilder = new StringBuilder();
		if (tomDate != null) {
			int[] daysMonthsYearsArray = getDaysMonthsYears(fomDate, tomDate, includeTomDate);

			if (daysMonthsYearsArray[2] > 0) {
				stringBuilder.append(daysMonthsYearsArray[2]).append(" �r");
			}
			if (daysMonthsYearsArray[2] > 0 && daysMonthsYearsArray[1] > 0) {
				stringBuilder.append(", ");
			}
			if (daysMonthsYearsArray[1] > 0) {
				stringBuilder.append(daysMonthsYearsArray[1]).append(" mnd.");
			}
		}
		return stringBuilder.toString();
	}

	/**
	 * Small utility method for comparing two dates down to the granularity of days (not milliseconds, which is the default
	 * behaviour in the standard API).
	 *
	 * @param thisDate the first date to check
	 * @param thatDate the second date to check
	 * @param allowSameDay if allowSameDay is true, the method returns true if thisDate is equal to thatDate with respect to year, month
	 * and day. If set to false, the method returns false on this condition
	 * @param isAfter set high for comparing after, false for comparing before
	 * @return if isAfter is true, returns true if thisDate is after thatDate, if isAfter is false, returns true of thisDate is
	 * before thatDate, false otherwise
	 */
	private static boolean compareDates(Date thisDate, Date thatDate, boolean allowSameDay, boolean isAfter) {
		Calendar thisCal = createDayCalendar(thisDate);
		Calendar thatCal = createDayCalendar(thatDate);

		if (allowSameDay && thisCal.equals(thatCal)) {
			return true;
		}
		if (isAfter) {
			return thisCal.after(thatCal);
		} else {
			return thisCal.before(thatCal);
		}
	}

	/**
	 * Create a calendar with only year, month and day set. If the passed date is null, it gets assigned to year zero,
	 * reasonably far from our time.
	 *
	 * @param date the date do convert
	 * @return the calendar of date granularity
	 */
	private static Calendar createDayCalendar(Date date) {
		Calendar cal = Calendar.getInstance();
		if (date == null) {
			cal.clear();
			cal.set(0, 0, 0, 0, 0, 0);
		} else {
			cal.setTime(date);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
		}
		return cal;
	}

	/**
	 * Extracts the period fields years, months and days to an int array.
	 *
	 * @param period the period to extract values from
	 * @return the updated duration array
	 */
	private static int[] formatPeriodToArray(Period period) {
		// Using Joda Time Period because it rocks

		int[] duration = {0, 0, 0};
		duration[2] += Math.abs(period.getYears());
		duration[1] += Math.abs(period.getMonths());
		int weeks = Math.abs(period.getWeeks());
		duration[0] += weeks * 7 + Math.abs(period.getDays());

		return duration;
	}

	/**
	 * Create IllegalArgumentException for null-date argument.
	 *
	 * @return IllegalArgumentException
	 */
	private static IllegalArgumentException createNullDateIsIllegalArgumentException() {
		return new IllegalArgumentException("null is a not valid input date");
	}

}