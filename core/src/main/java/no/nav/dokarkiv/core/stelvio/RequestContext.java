package no.nav.dokarkiv.core.stelvio;

/**
 * Has information that should be accessible anywhere in the system for a single request. Implementations decide how to
 * get to/store the information necessary for providing this functionality.
 *
 * @author Stig Kleppe-Jørgensen, Accenture
 * @author Morten Andersen-Gott, Accenture
 */
@Deprecated
public interface RequestContext {

	/**
	 * Get the id of the screen that started the process that is currently executing. The id
	 * should be informative and describe the name of the screen, e.g. "TaskList" and "Customer".
	 *
	 * @return The id of the current screen, null if not user initiated
	 */
	String getScreenId();

	/**
	 * The usage of this field is to be decided later.
	 * Do not assume this field is available. It may  be removed if it's found to be of no use.
	 *
	 * @return String TBA
	 */
	String getModuleId();

	/**
	 * Get the id or description of the process.
	 * @return the processId
	 */
	String getProcessId();
	
	/**
	 * Get the unique id of the transaction that is currently executing. The id is typically
	 * a unique number or string, and not a descriptive name. Current user may initiate the same
	 * process more than one time, but the transaction id will typically be unique per execution.
	 *
	 * @return The id of the current transaction.
	 */
	String getTransactionId();
	
	/**
	 * Get the id identifying the component (application). There will typically be one id configured
	 * for one EAR or WAR
	 * @return The id as a String
	 */
	String getComponentId();
	
	/**
	 * Get the id identifying user that initiated the call.
	 * @return The userId as a String
	 */
	String getUserId();
	
}
