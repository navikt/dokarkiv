package no.nav.dokarkiv.core.jaxws;

import com.google.common.collect.Sets;
import jakarta.xml.soap.Node;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPHeader;
import jakarta.xml.ws.ProtocolException;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPHandler;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import static no.nav.dokarkiv.core.MDCConstants.MDC_APP_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_CALL_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;

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
			if (log.isDebugEnabled()) {
				log.debug("About to extract appId, callId and consumerId from SOAP message");
			}
			SOAPHeader header = null;
			try {
				header = context.getMessage().getSOAPHeader();
			} catch (SOAPException e) {
				throw new ProtocolException(e);
			}

			MDC.put(MDC_APP_ID, extractMDC(header, APPID_QNAME));
			MDC.put(MDC_CONSUMER_ID, extractMDC(header, CONSUMER_QNAME));
			String callId = extractMDC(header, CALLID_QNAME);
			MDC.put(MDC_CALL_ID, callId.isEmpty() ? UUID.randomUUID().toString() : callId);

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

	private String extractMDC(SOAPHeader header, QName mdcQname) {
		String mdc = "";
		if (header == null) {
			return mdc;
		}
		Iterator<Node> headersIter = header.getChildElements(mdcQname);
		while (headersIter.hasNext()) {
			Node element = headersIter.next();
			if (CALLID_QNAME.getNamespaceURI().equals(element.getNamespaceURI()) &&
					CALLID_QNAME.getLocalPart().equals(element.getLocalName())) {
				mdc = element.getValue();
				if (log.isDebugEnabled()) {
					log.debug("Found " + mdcQname + " : " + mdc);
				}
				break;
			}
		}
		return mdc == null ? "" : mdc;
	}
}
