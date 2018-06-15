package no.nav.dokarkiv.core.stelvio;

import org.slf4j.MDC;

/**
 * MDC constants.
 * <p>
 * Defines MDC property keys than can be used by slf4j to log additional context data to a log entry. See {@link MDC}. slf4j
 * loggers can use these properties to set corresponding values. If the MDC properties are set with values, slf4j can be
 * configured to include them in the logs by specifying the <code>ConversionPattern</code> on the layout. Example:
 * <code>[appender].layout.ConversionPattern=... %X{user}... </code>
 * </p>
 * 
 * @author Kjetil Kristiansen
 * @see RequestContext Contains most of the context data applicable to MDC logging.
 */
public class MdcConstants {

	/**
	 * Application key that identifies the application component. Typically from {@link RequestContext#getComponentId()}.
	 */
	public static final String MDC_APPLICATION = "applicationKey";

	/**
	 * Error id, used in error logging to provide framework exception error ids. Typically from
	 * {@link StelvioException#getErrorId()}.
	 */
	public static final String MDC_ERROR = "error";

	/**
	 * Error type, used in error logging to specify wether the logged exception is of technical or functional nature.
	 */
	public static final String MDC_ERROR_TYPE = "errorType";

	/**
	 * Error type value for technical/operational errors.
	 */
	public static final String MDC_ERROR_TYPE_TECHNICAL_VALUE = "Technical";

	/**
	 * Error type value for functional errors.
	 */
	public static final String MDC_ERROR_TYPE_FUNCTIONAL_VALUE = "Functional";

	/**
	 * Id of Module (Currently undefined what this should be). Typically from {@link RequestContext#getModuleId()}.
	 */
	public static final String MDC_MODULE = "module";

	/**
	 * Id of the currently running process. Typically from {@link RequestContext#getProcessId()}.
	 */
	public static final String MDC_PROCESS = "process";

	/**
	 * Id of the screen that started the process that is currently executing. Typically from
	 * {@link RequestContext#getScreenId()}.
	 */
	public static final String MDC_SCREEN = "screen";

	/**
	 * Unique id of the transaction that is currently executing. . Typically from {@link RequestContext#getTransactionId()}.
	 */
	public static final String MDC_TRANSACTION = "transaction";

	/**
	 * The id identifying user that initiated the method call. Typically from {@link RequestContext#getUserId()}.
	 */
	public static final String MDC_USER = "user";

}
