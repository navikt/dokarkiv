package no.nav.dokarkiv.core.consumer;

import no.nav.dokarkiv.core.stelvio.FunctionalRecoverableException;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * Local implementation of stelvio FunctionalRecoverableException.
 *
 * @author Steinar Hodnebo
 * @author Bent Are Melsom
 */
public abstract class ConsumerRecoverableException extends FunctionalRecoverableException {
	
	/** Serialization UID */
	private static final long serialVersionUID = -***gammelt_fnr***6205603L;
	private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerRecoverableException.class);
	
	String errorMessage;
	String errorSource;
	String errorType;
	String rootCause;
	String dateTimeStamp;

	/**
	 * @param cause
	 *            Throwable.
	 * @param message
	 *            the message to display
	 */
	protected ConsumerRecoverableException(Throwable cause, String message) {
		super(message, cause);

		try {
			BeanUtils.copyProperties(this, cause);
		} catch (IllegalAccessException | InvocationTargetException e) {
			LOGGER.error(e.getMessage(), e);
		}

		setDetailedMessageInThrowable(cause);
	}

	private void setDetailedMessageInThrowable(Throwable cause) {
		if (this.errorMessage != null) {
			try {
				Field detailMessageField = Throwable.class.getDeclaredField("detailMessage");
				detailMessageField.setAccessible(true);
				detailMessageField.set(cause,
					"ErrorMessage= " + this.errorMessage + ", RootCause= " + this.rootCause + ", DateTimeStamp= "
					+ this.dateTimeStamp);
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			} 
		}
	}

	/**
	 * Getter for the errorMessage property.
	 *
	 * @return the errorMessage
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * Setter for the errorMessage property.
	 *
	 * @param errorMessage the errorMessage to set
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/**
	 * Getter for the errorSource property.
	 *
	 * @return the errorSource
	 */
	public String getErrorSource() {
		return errorSource;
	}

	/**
	 * Setter for the errorSource property.
	 *
	 * @param errorSource the errorSource to set
	 */
	public void setErrorSource(String errorSource) {
		this.errorSource = errorSource;
	}

	/**
	 * Getter for the errorType property.
	 *
	 * @return the errorType
	 */
	public String getErrorType() {
		return errorType;
	}

	/**
	 * Setter for the errorType property.
	 *
	 * @param errorType the errorType to set
	 */
	public void setErrorType(String errorType) {
		this.errorType = errorType;
	}

	/**
	 * Getter for the rootCause property.
	 *
	 * @return the rootCause
	 */
	public String getRootCause() {
		return rootCause;
	}

	/**
	 * Setter for the rootCause property.
	 *
	 * @param rootCause the rootCause to set
	 */
	public void setRootCause(String rootCause) {
		this.rootCause = rootCause;
	}

	/**
	 * Getter for the dateTimeStamp property.
	 *
	 * @return the dateTimeStamp
	 */
	public String getDateTimeStamp() {
		return dateTimeStamp;
	}

	/**
	 * Setter for the dateTimeStamp property.
	 *
	 * @param dateTimeStamp the dateTimeStamp to set
	 */
	public void setDateTimeStamp(String dateTimeStamp) {
		this.dateTimeStamp = dateTimeStamp;
	}

}
