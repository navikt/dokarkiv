package no.nav.dokarkiv.core.audit;

import no.nav.dokarkiv.core.stelvio.RequestContext;
import no.nav.dokarkiv.core.stelvio.RequestContextHolder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * AuditItem holds information that should be logged when a system that is subject to audit is accessed.
 * 
 * @author Morten Andersen-Gott (Accenture)
 */
public class AuditItem {

	private LocalDateTime timestamp;

	private String source;

	private String target;

	private String transactionId;

	private String userId;

	private String message;

	private AccessType accessType;

	private ProtectionLevel protectionLevel;

	private Map<String, String> customInfo = new HashMap<String, String>();

	/**
	 * Constructor that creates an <code>{@link AuditItem}</code> Setting userId and transactionId from RequestContext if
	 * RequestContext is bound to thread. Source is set to the componentId specified bythe RequestContext if RequestContext is
	 * set.
	 * 
	 */
	public AuditItem() {
		timestamp = LocalDateTime.now();
		if (RequestContextHolder.isRequestContextSet()) {
			userId = RequestContextHolder.currentRequestContext().getUserId();
			transactionId = RequestContextHolder.currentRequestContext().getTransactionId();
			source = RequestContextHolder.currentRequestContext().getComponentId();
		}
	}

	/**
	 * Adds custom information to this audit event. for each added information element a unique key describing the information
	 * must be provided
	 * 
	 * @param key
	 *            - unique key for this custom informaion, describing the value (eg. Person.fnr)
	 * @param customInfo
	 *            - the custom information as a string
	 */
	public void addCustomInfo(String key, String customInfo) {
		this.customInfo.put(key, customInfo);
	}

	/**
	 * Returns a read-only map with the custom information.
	 * 
	 * @return read only map with describing property-key and property value
	 */
	public Map<String, String> getCustomInfo() {
		return Collections.unmodifiableMap(customInfo);
	}

	/**
	 * Gets the message describing the event that is triggering auditing.
	 * 
	 * @return message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Gets the message describing the event.
	 * 
	 * @param message
	 *            message
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Gets the source that triggered the event (Name of the application/component and/or service/method/screenId).
	 * 
	 * @return source
	 */
	public String getSource() {
		return source;
	}

	/**
	 * Sets the source that triggered the event (Name of the application/component and/or service/method/screenId).
	 * 
	 * @param source
	 *            source
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * Gets the name of the target system (Name of the component and/or service/method that is being accessed).
	 * 
	 * @return target
	 */
	public String getTarget() {
		return target;
	}

	/**
	 * Sets the name of the target system (Name of the component and/or service/method that is being accessed).
	 * 
	 * @param target
	 *            target
	 */
	public void setTarget(String target) {
		this.target = target;
	}

	/**
	 * Gets the timestamp for when the auditable event was triggered.
	 * 
	 * @return timstamp as Date
	 */
	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	/**
	 * Sets the timestamp for when the auditable event was triggered.
	 * 
	 * @param timestamp
	 *            time stamp as Date
	 */
	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Gets the unique transaction Id for the request The transactionId of the {@link RequestContext} should typically be used.
	 * 
	 * @return transactionId as String
	 */
	public String getTransactionId() {
		return transactionId;
	}

	/**
	 * Sets the unique transaction Id for the request The transactionId of the {@link RequestContext} should typically be used.
	 * 
	 * If {@link RequestContextHolder#isRequestContextSet()} is true, the transactionId will already have been set in the
	 * constructor.
	 * 
	 * @param transactionId
	 *            id as String
	 */
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	/**
	 * Gets the userId of the user calling the auditable service or sub system.
	 * 
	 * @return userId as String
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * Sets the userId of the user calling the auditable service or sub system.
	 * 
	 * The userId of the {@link RequestContext} should typically be used.
	 * 
	 * If {@link RequestContextHolder#isRequestContextSet()} is true, the userId will already have been set in the constructor.
	 * 
	 * @param userId
	 *            id as String
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("AccessType=").append(getAccessType()).append(", ");
		sb.append("ProtectionLevel=").append(getProtectionLevel()).append(", ");
		sb.append("Message=").append(getMessage()).append(", ");
		sb.append("Target=").append(getTarget()).append(", ");
		sb.append("Source=").append(getSource()).append(", ");
		sb.append("UserId=").append(getUserId()).append(", ");
		sb.append("Timestamp=").append(getTimestamp()).append(", ");
		sb.append("CustomInfo=").append(parseCustomInfo(getCustomInfo()));
		return sb.toString();
	}

	/**
	 * Utility method, used to create a loggable string of the customInfo.
	 * 
	 * @param customInfo
	 *            custom info
	 * @return custom info string
	 */
	private String parseCustomInfo(Map<String, String> customInfo) {
		StringBuilder sb;
		sb = customInfo.isEmpty() ? new StringBuilder("null") : new StringBuilder("[");
		for (Map.Entry<String, String> entry : customInfo.entrySet()) {
			// Add , as separator if StringBuilder contains data
			if (sb.indexOf("=") != -1) {
				sb.append(", ");
			}
			sb.append(entry.getKey()).append("=").append(entry.getValue());
		}

		if (sb.indexOf("[") != -1) {
			sb.append("]");
		}

		return sb.toString();
	}

	/**
	 * Gets the accessType, as specified by <code>{@link AccessType}</code>.
	 * 
	 * @return accessType
	 */
	public AccessType getAccessType() {
		return accessType;
	}

	/**
	 * Sets the access type, as specified by <code>{@link AccessType}</code>.
	 * 
	 * @param accessType
	 *            access type
	 */
	public void setAccessType(AccessType accessType) {
		this.accessType = accessType;
	}

	/**
	 * Gets the protection level as specified by <code>{@link ProtectionLevel}</code>.
	 * 
	 * @return protectionLevel
	 */
	public ProtectionLevel getProtectionLevel() {
		return protectionLevel;
	}

	/**
	 * Sets the protection level as specified by <code>{@link ProtectionLevel}</code>.
	 * 
	 * @param protectionLevel
	 *            protection level
	 */
	public void setProtectionLevel(ProtectionLevel protectionLevel) {
		this.protectionLevel = protectionLevel;
	}

	/**
	 * Enum which holds the different protection level values that can be set on an {@link AuditItem}.
	 * 
	 * @author Morten Andersen-Gott (Accenture)
	 * 
	 */
	public enum ProtectionLevel {

		/*
		 * NB! KEEP LOWEST PROTECTION LEVEL AT TOP, INCREASING TO THE HIGHEST LEVEL AT THE BOTTOM. THIS WILL MAKE THE
		 * COMPARETO-METHOD WORK AS INTENDED. IT IS FINAL IN ENUMS AND IMPLEMENTED ACCORDING TO ORDINAL
		 */

		/**
		 * Indicates that the data that was accessed/updated is protected with protection level low.
		 * 
		 * Corresponds to PIB value LAV.
		 */
		LOW,

		/**
		 * Indicates that the data that was accessed/updated is protected with protection level medium.
		 * 
		 * Corresponds to PIB value MEDIUM.
		 */
		MEDIUM,

		/**
		 * Indicates that the data that was accessed/updated is protected with protection level high.
		 * 
		 * Corresponds to PIB value HØY.
		 */
		HIGH,

		/**
		 * Indicates that the data that was accessed/updated is protected with protection level very high.
		 * 
		 * Corresponds to PIB value EKSTRA HØY.
		 * 
		 */
		EXTRA_HIGH,

		/**
		 * Indicates that the data that was accessed/updated is protected with protection level very high.
		 * 
		 * Corresponds to PIB value SVÆRT HØY.
		 * 
		 */
		SUPER_HIGH;

		/**
		 * Checks if <code>this</code> protectionlevel is considered higher than the supplied value.
		 * 
		 * @param protectionLevel
		 *            instance
		 * @return <code>true</code> if this is higher, otherwise <code>false</code>, equal levels will return false
		 */
		public boolean isHigherThan(ProtectionLevel protectionLevel) {
			return this.compareTo(protectionLevel) > 0;
		}

	}

	/**
	 * Enum which holds the different protection level values that can be set on an {@link AuditItem}.
	 * 
	 * @author Morten Andersen-Gott (Accenture)
	 * 
	 */
	public enum AccessType {
		/**
		 * Data is being accessed by a read operation.
		 */
		AUDIT_READ,

		/**
		 * Data is being created, updated or deleted.
		 */
		AUDIT_WRITE;
	}

}
