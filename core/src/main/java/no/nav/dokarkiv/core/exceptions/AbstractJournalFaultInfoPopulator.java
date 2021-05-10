package no.nav.dokarkiv.core.exceptions;

import no.nav.dokarkiv.core.domain.util.DateProvider;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Abstract base class for faultinfo populators.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public abstract class AbstractJournalFaultInfoPopulator {

	private static final String COMPONENT_NAME = "JOARK";

	/**
	 * Get the errorsource.
	 *
	 * @param operationName The operation that failed
	 * @return The errorSource
	 */
	protected String getErrorSource(String operationName) {
		return COMPONENT_NAME + ":" + operationName;
	}

	/**
	 * Get the root cause of an exception.
	 *
	 * @param exception The exception to get the root for
	 * @return The root cause
	 */
	protected Throwable getRootCause(Exception exception) {
		boolean done = false;
		Throwable throwable = exception;
		while (!done) {
			if (throwable.getCause() == null) {
				done = true;
			} else {
				throwable = throwable.getCause();
			}
		}
		return throwable;
	}

	/**
	 * Get current time as XMLGregorianCalendar
	 *
	 * @return now
	 */
	protected XMLGregorianCalendar getXmlTimestamp() {
		GregorianCalendar calendar = new GregorianCalendar();
		// Setting the date explicitly to make it testable
		calendar.setTime(DateProvider.getToday());
		try {
			return DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
		} catch (DatatypeConfigurationException e) {
			throw new ApplicationException("Unable to create XMLGregorianCalendar", e);
		}
	}

	protected DateTime getTodayJodaTime() {
		Date today = DateProvider.getToday();
		return new DateTime(today.getTime());
	}
}
