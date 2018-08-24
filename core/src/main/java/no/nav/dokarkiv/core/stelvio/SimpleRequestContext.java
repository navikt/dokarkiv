package no.nav.dokarkiv.core.stelvio;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * This class is used to store and retrieve information that must be available anywhere in the system during a single request.
 * The population of the different fields in the RequestContext is left to the outmost component (normally the Presentation
 * Framework).
 * 
 * All transaction specific information is stored for the current thread. Again, it is the outmost component responsibility to
 * populate and remove the data from the thread.
 * 
 * While working on different tiers, the RequestContext must be exported in one tier, and then imported back again on the other
 * tier.
 *
 * @deprecated reduser antall felt til de vi faktisk bruker
 * @author Petter Skodvin, Accenture
 * @author Stig Kleppe-Jørgensen, Accenture
 * @version $Id: RequestContext.java 1979 2005-02-16 16:34:40Z psa2920 $
 */
@Deprecated
public final class SimpleRequestContext implements RequestContext, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = ***gammelt_fnr***7471488L;

	private String screenId;
	private String moduleId;
	private String transactionId;
	private String componentId;
	private String userId;
	private String processId;

	/**
	 * Creates a new instance of SimpleRequestContext.
	 */
	public SimpleRequestContext() {
	}

	/**
	 * Initializes the instance with the context specified.
	 * 
	 * @param screenId
	 *            the current screen id.
	 * @param moduleId
	 *            the current module id.
	 * @param transactionId
	 *            the current transaction id.
	 * @param componentId
	 *            the current component id.
	 * @param userId
	 *            the current user id.
	 */
	public SimpleRequestContext(String screenId, String moduleId, String transactionId, String componentId, String userId) {
		this(screenId, moduleId, transactionId, componentId, userId, null);
	}

	/**
	 * Initializes the instance with the context specified.
	 * 
	 * @param screenId
	 *            the current screen id.
	 * @param moduleId
	 *            the current module id.
	 * @param transactionId
	 *            the current transaction id.
	 * @param componentId
	 *            the current component id.
	 * @param userId
	 *            the current user id.
	 * @param processId
	 *            the id or description of the process
	 */
	public SimpleRequestContext(String screenId, String moduleId, String transactionId, String componentId, String userId,
								String processId) {
		super();
		this.screenId = screenId;
		this.moduleId = moduleId;
		this.transactionId = transactionId;
		this.componentId = componentId;
		this.userId = userId;
		this.processId = processId;
	}

	/**
	 * Initializes the instance with the context specified. UserId is set to <code>null</code>
	 * 
	 * @param screenId
	 *            the current screen id.
	 * @param moduleId
	 *            the current module id.
	 * @param transactionId
	 *            the current transaction id.
	 * @param componentId
	 *            the current component id.
	 */
	public SimpleRequestContext(String screenId, String moduleId, String transactionId, String componentId) {
		this(screenId, moduleId, transactionId, componentId, null);
	}

	@Override
	public String getScreenId() {
		return screenId;
	}

	@Override
	public String getModuleId() {
		return moduleId;
	}

	@Override
	public String getTransactionId() {
		return transactionId;
	}

	@Override
	public String getComponentId() {
		return componentId;
	}

	@Override
	public String getUserId() {
		return userId;
	}

	/**
	 * toString implementation. Returns a String the default toString and appends a representation of all fields and their name.
	 * Example of a toString: no.stelvio.common.context.support.SimpleRequestContext@74827482
	 * [transactionId=a8771a7d-77ae-493d-959b-4b52abf4d9c7,componentId=PSAK,screenId=<null>, userId=<null>,moduleId=<null>]
	 * 
	 * @return string
	 */
	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("transactionId", transactionId);
		builder.append("componentId", componentId);
		builder.append("screenId", screenId);
		builder.append("userId", userId);
		builder.append("moduleId", moduleId);
		builder.append("processId", processId);
		return builder.toString();
	}

	/**
	 * Return the processId.
	 * 
	 * @return process id
	 */
	public String getProcessId() {
		return processId;
	}

	/**
	 * Implementation of the Builder pattern to more easily build a <code>RequestContext</code>, especially when having to
	 * create a new version based on another.
	 */
	public static class Builder {
		private String screenId;
		private String moduleId;
		private String transactionId;
		private String userId;
		private String componentId;
		private String processId;

		/**
		 * Constructor.
		 */
		public Builder() {
		}

		/**
		 * Constructor.
		 * 
		 * @param requestContext
		 *            a RequestContext
		 */
		public Builder(RequestContext requestContext) {
			moduleId = requestContext.getModuleId();
			screenId = requestContext.getScreenId();
			transactionId = requestContext.getTransactionId();
			componentId = requestContext.getComponentId();
			userId = requestContext.getUserId();
			processId = requestContext.getProcessId();
		}

		/**
		 * Sets the moduleId and returns the object.
		 * 
		 * @param moduleId
		 *            the moduleId
		 * @return the object
		 */
		public Builder moduleId(String moduleId) {
			this.moduleId = moduleId;

			return this;
		}

		/**
		 * Sets the screenId and returns the object.
		 * 
		 * @param screenId
		 *            the screenId
		 * @return the object
		 */
		public Builder screenId(String screenId) {
			this.screenId = screenId;

			return this;
		}

		/**
		 * Sets the transactionId and returns the object.
		 * 
		 * @param transactionId
		 *            the transactionId
		 * @return the object
		 */
		public Builder transactionId(String transactionId) {
			this.transactionId = transactionId;

			return this;
		}

		/**
		 * Sets the userId and returns the object.
		 * 
		 * @param userId
		 *            the userId
		 * @return the object
		 */
		public Builder userId(String userId) {
			this.userId = userId;

			return this;
		}

		/**
		 * Sets the componentId and returns the object.
		 * 
		 * @param componentId
		 *            the componentId
		 * @return the object
		 */
		public Builder componentId(String componentId) {
			this.componentId = componentId;

			return this;
		}

		/**
		 * Sets the processId and returns the object.
		 * 
		 * @param processId
		 *            process id
		 * @return builder
		 */
		public Builder processId(String processId) {
			this.processId = processId;
			return this;
		}

		/**
		 * Returns a new SimpleRequestContext using the variables from the class.
		 * 
		 * @return new SimpleRequestContext
		 */
		public SimpleRequestContext build() {
			return new SimpleRequestContext(screenId, moduleId, transactionId, componentId, userId, processId);
		}
	}

	/**
	 * Only for test purposes, use the builder for changing the attributes.
	 *
	 * @param screenId the screenId to set
	 */
	public void setScreenId(String screenId) {
		this.screenId = screenId;
	}

	/**
	 * Only for test purposes, use the builder for changing the attributes.
	 *
	 * @param moduleId the moduleId to set
	 */
	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	/**
	 * Only for test purposes, use the builder for changing the attributes.
	 *
	 * @param transactionId the transactionId to set
	 */
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	/**
	 * Only for test purposes, use the builder for changing the attributes.
	 *
	 * @param componentId the componentId to set
	 */
	public void setComponentId(String componentId) {
		this.componentId = componentId;
	}

	/**
	 * Set the userId, use the builder for changing the attributes.
	 *
	 * @param userId the userId to set
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	/**
	 * Only for test purposes, use the builder for changing the attributes.
	 *
	 * @param processId the processId to set
	 */
	public void setProcessId(String processId) {
		this.processId = processId;
	}
}
