package no.nav.dokarkiv.arkiverdokumentmottak;


import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.exceptions.ApplicationException;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

/**
 * Abstract base class for faultinfo populators.
 *
 * @author Thomas Eugen Bj�rge, Visma Sirius
 */
public abstract class AbstractJournalFaultInfoPopulator {

	private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	private static final String COMPONENT_NAME = "JOARK";
	protected static final String ERROR_TYPE = "Business";

	/**
	 * Get the errorsource.
	 *
	 * @param operationName The operation that failed
	 * @return The errorSource
	 */
	protected String getErrorSource(String operationName) {
		return new StringBuilder(COMPONENT_NAME).append(":").append(operationName).toString();
	}

	/**
	 * Get the current time, formatted.
	 *
	 * @return The current time
	 */
	protected String getTimeStamp() {
		return new SimpleDateFormat(DATE_PATTERN).format(DateProvider.getToday());
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
			if (throwable.getCause() != null) {
				throwable = throwable.getCause();
			} else {
				done = true;
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

}
