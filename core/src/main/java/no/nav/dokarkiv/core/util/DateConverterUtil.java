package no.nav.dokarkiv.core.util;

import no.nav.dokarkiv.core.exceptions.DokarkivTechnicalException;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Utility class for date conversion between domain and ws-transfer objects
 *
 * (Mostly copied from pesys' DateUtils)
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
public final class DateConverterUtil {

	private DateConverterUtil() {
	}

	/**
	 * Null-safe date converter
	 * from {@link javax.xml.datatype.XMLGregorianCalendar}
	 * to   {@link java.util.Date}
	 *
	 * @param source of type <code>XMLGregorianCalendar</code>
	 * @return the <code>Date</code> representing <tt>source</tt>
	 */
	public static Date convertXMLGregorianCalendarToDate(XMLGregorianCalendar source) {
		return source == null ? null : source.toGregorianCalendar().getTime();
	}

	/**
	 * Null-safe XMLGregorianCalendar converter
	 * from {@link java.util.Date}
	 * to {@link javax.xml.datatype.XMLGregorianCalendar}
	 *
	 * @param source date of type {@link java.util.Date}
	 * @return <code>XMLGregorianCalendar</code> representing <tt>source</tt>
	 */
	public static XMLGregorianCalendar convertDateToXMLGregorianCalendar(Date source) {
		return convertDateToXMLGregorianCalendar(source, true);
	}


	/**
	 * Converter
	 * from {@link java.util.Date}
	 * to {@link javax.xml.datatype.XMLGregorianCalendar}
	 *
	 * @param source date of type {@link java.util.Date}
	 * @param nullable <code>true</code> if Date source can be null, otherwise <code>false</code>
	 * @return <code>XMLGregorianCalendar</code> representing <tt>source</tt>
	 */
	public static XMLGregorianCalendar convertDateToXMLGregorianCalendar(Date source, boolean nullable) {
		XMLGregorianCalendar xgc = null;
		if (!nullable || source != null) {
			GregorianCalendar gc = new GregorianCalendar();
			gc.clear();
			gc.setTime(source);

			try {
				xgc = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
			} catch (DatatypeConfigurationException e) {
				throw new DokarkivTechnicalException("Failed to convertFilType date '" + source + "'", e);
			}
		}
		return xgc;
	}
}
