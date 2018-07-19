package no.nav.dokarkiv.core.stelvio;

import java.util.ArrayList;

/**
 * Exception thrown if one or more required properties are not set by configuration.
 * 
 * @author Morten Andersen-Gott, Accenture
 */
public class MissingPropertyException extends ConfigurationException {

	private static final long serialVersionUID = ***gammelt_fnr***65754897L;

	private Object[] missingProperties;

	/**
	 * Constructs a <code>MissingPropertyException</code> with missing properties and message.
	 * 
	 * @param message
	 *            - the exception message.
	 * @param missingProperties
	 *            - the names of the missing properties.
	 */
	public MissingPropertyException(String message, ArrayList<String> missingProperties) {
		this(message, missingProperties, null);
	}

	/**
	 * Constructs a <code>MissingPropertyException</code> with missing properties and message.
	 * 
	 * @param message
	 *            - the exception message.
	 * @param missingProperties
	 *            - the names of the missing properties.
	 */
	public MissingPropertyException(String message, String[] missingProperties) {
		this(message, missingProperties, null);
	}

	/**
	 * Constructs a <code>MissingPropertyException</code> with missing properties, message and cause.
	 * 
	 * @param message
	 *            - the exception message.
	 * @param missingProperties
	 *            - the names of the missing properties.
	 * @param cause
	 *            - the throwable that caused the exception to be raised.
	 */
	public MissingPropertyException(String message, ArrayList<String> missingProperties, Throwable cause) {
		super(message + ". " + createMissingPropertiesString(missingProperties), cause);
		if (missingProperties != null) {
			this.missingProperties = (String[]) missingProperties.toArray(new String[missingProperties.size()]);
		}
	}

	/**
	 * Constructs a <code>MissingPropertyException</code> with missing properties, message and cause.
	 * 
	 * @param message
	 *            - the exception message.
	 * @param missingProperties
	 *            - the names of the missing properties.
	 * @param cause
	 *            - the throwable that caused the exception to be raised.
	 */
	public MissingPropertyException(String message, String[] missingProperties, Throwable cause) {
		super(message + ". " + createMissingPropertiesString(missingProperties), cause);
		this.missingProperties = missingProperties.clone();
	}

	/**
	 * Utility-method to create a String representation of the template arguments.
	 * 
	 * @param missingProperties
	 *            missing properties
	 * @return properties string
	 */
	private static String createMissingPropertiesString(Object[] missingProperties) {
		StringBuilder sb = new StringBuilder("Missing properties:");
		for (Object object : missingProperties) {
			sb.append(" ");
			sb.append(object.toString());
		}
		return sb.toString();
	}

	/**
	 * Utility-method to create a String representation of the template arguments.
	 * 
	 * @param missingProperties
	 *            missing properties
	 * @return properties string
	 */
	private static String createMissingPropertiesString(ArrayList<String> missingProperties) {
		StringBuilder sb = new StringBuilder();
		if (missingProperties != null) {
			sb.append(createMissingPropertiesString((String[]) missingProperties.toArray(new String[missingProperties
					.size()])));
		}

		return sb.toString();
	}

	/**
	 * Gets missing properties.
	 * 
	 * @return List of missing properties
	 */
	public Object[] getMissingProperties() {
		return missingProperties;
	}

}
