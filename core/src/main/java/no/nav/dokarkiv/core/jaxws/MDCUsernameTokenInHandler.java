package no.nav.dokarkiv.core.jaxws;

import static no.nav.dokarkiv.core.jaxws.MDCConstants.MDC_APP_ID;
import static no.nav.dokarkiv.core.jaxws.MDCConstants.MDC_CALL_ID;
import static no.nav.dokarkiv.core.jaxws.MDCConstants.MDC_CONSUMER_ID;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Iterator;
import java.util.Set;

/**
 * Handler used for UsernameToken to get appId, consumerId and callId, which is not present in the
 * username-token
 *
 * @author Stig Strøm
 */
public class MDCUsernameTokenInHandler implements SOAPHandler<SOAPMessageContext> {

	private static final String URI_NO_NAV_APPLIKASJONSRAMMEVERK = "uri:no.nav.applikasjonsrammeverk";

	private static final Logger log = LoggerFactory.getLogger(MDCUsernameTokenInHandler.class);

	static final QName APPID_QNAME = new QName(URI_NO_NAV_APPLIKASJONSRAMMEVERK, MDC_APP_ID);
	static final QName CONSUMER_QNAME = new QName(URI_NO_NAV_APPLIKASJONSRAMMEVERK, MDC_CONSUMER_ID);
	static final QName CALLID_QNAME = new QName(URI_NO_NAV_APPLIKASJONSRAMMEVERK, MDC_CALL_ID);


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean handleMessage(SOAPMessageContext context) {
		Boolean outbound = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		// INBOUND processing
		if (!outbound) {
			log.debug("About to extract appId, callId and consumerId from SOAP message");
			SOAPHeader header = null;
			try {
				header = context.getMessage().getSOAPHeader();
			} catch (SOAPException e) {
				throw new ProtocolException(e);
			}

			MDC.put(MDC_APP_ID, extractMDC(header, APPID_QNAME));
			MDC.put(MDC_CONSUMER_ID, extractMDC(header, CONSUMER_QNAME));
			MDC.put(MDC_CALL_ID, extractMDC(header, CALLID_QNAME));

		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean handleFault(SOAPMessageContext context) {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close(MessageContext context) {
		MDC.remove(MDC_APP_ID);
		MDC.remove(MDC_CALL_ID);
		MDC.remove(MDC_CONSUMER_ID);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<QName> getHeaders() {
		return Sets.newHashSet(APPID_QNAME, CONSUMER_QNAME, CALLID_QNAME);
	}

	@SuppressWarnings("unchecked")
	private String extractMDC(SOAPHeader header, QName mdcQname) {
		String mdc = "";
		if (header == null) {
			return mdc;
		}
		Iterator<SOAPElement> headersIter = header.getChildElements(mdcQname);
		while (headersIter.hasNext()) {
			SOAPElement element = headersIter.next();
			if (element.getElementQName().equals(mdcQname)) {
				mdc = element.getValue();
				log.debug("Found " + mdcQname + " : " + mdc);
				break;
			}
		}
		return mdc;
	}

}
