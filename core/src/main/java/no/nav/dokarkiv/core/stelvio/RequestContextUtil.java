package no.nav.dokarkiv.core.stelvio;


import static no.nav.dokarkiv.core.MDCConstants.MDC_CALL_ID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.xml.ws.WebServiceContext;
import java.security.Principal;
import java.util.UUID;

/**
 * Class that contains functionality to create and set a RequestContext for the current web service request.
 * <p>
 * NB! CallIdHandler must be configured in order to make callId available in MDC.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
public final class RequestContextUtil {

	private static final Logger logger = LoggerFactory.getLogger(RequestContextUtil.class);

	private RequestContextUtil() {
	}

	/**
	 * Create and set the RequestContext.
	 *
	 * @param username The user Principal.
	 * @param applikasjonsID The applikasjonsID from the web service request.
	 */
	public static void createAndSetUsername(String username, String applikasjonsID) {
		if (StringUtils.isBlank(applikasjonsID)) {
			throw new IllegalArgumentException("ApplikasjonsID must be set on the request");
		}

		SimpleRequestContext requestContext = new SimpleRequestContext
				.Builder()
				.userId(username)
				.componentId(applikasjonsID)
				.transactionId(getCallId())
				.build();

		if (logger.isDebugEnabled()) {
			logger.debug("Setting RequestContext " + requestContext);
		}

		RequestContextSetter.setRequestContext(requestContext);
	}

	/**
	 * Create and set the RequestContext.
	 *
	 * @param webServiceContext The context containing the user Principal.
	 * @param applikasjonsID The applikasjonsID from the web service request.
	 */
	public static void createAndSetRequestContext(WebServiceContext webServiceContext, String applikasjonsID) {
		createAndSetUsername(getUserId(webServiceContext), applikasjonsID);
	}

	private static String getUserId(WebServiceContext webServiceContext) {
		Principal userPrincipal = webServiceContext.getUserPrincipal();
		if (userPrincipal == null) {
			throw new IllegalArgumentException("No user Principal found, make sure policy set is configured corretly");
		}
		return userPrincipal.getName();
	}

	private static String getCallId() {
		String callId = MDC.get(MDC_CALL_ID);
		if(callId == null) {
			return UUID.randomUUID().toString();
		} else {
			return callId;
		}
	}

}
