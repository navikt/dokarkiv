package no.nav.dokarkiv.core.exceptions;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.ZonedDateTime;
import java.util.GregorianCalendar;

/**
 * Abstract base class for faultinfo populators.
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
		GregorianCalendar calendar = GregorianCalendar.from(ZonedDateTime.now());
		try {
			return DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
		} catch (DatatypeConfigurationException e) {
			throw new ApplicationException("Unable to create XMLGregorianCalendar", e);
		}
	}
}
