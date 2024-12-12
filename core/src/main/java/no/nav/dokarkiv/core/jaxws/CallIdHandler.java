package no.nav.dokarkiv.core.jaxws;

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
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CALL_ID;

/**
 * Inspired by MDCInHandler in modig-log-jaxws. Since MDCInHandler uses
 * SubjectHandler which in turn expects SAML security we can not use it.
 * Instead we use this class that just handles callId.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
public class CallIdHandler implements SOAPHandler<SOAPMessageContext> {

	private static final Logger logger = LoggerFactory.getLogger(CallIdHandler.class);

	// QName for the callId header
	private static final QName CALLID_QNAME = new QName("uri:no.nav.applikasjonsrammeverk", MDC_CALL_ID);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean handleMessage(SOAPMessageContext context) {
		Boolean outbound = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		// INBOUND processing
		if (!outbound) {
			if (logger.isDebugEnabled()) {
				logger.debug("About to extract callId from SOAP message");
			}
			SOAPHeader header = null;
			try {
				header = context.getMessage().getSOAPHeader();
			} catch (SOAPException e) {
				throw new ProtocolException(e);
			}
			String callId = extractCallId(header);

			MDC.put(MDC_CALL_ID, callId);
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
		//noop
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<QName> getHeaders() {
		return Collections.singleton(CALLID_QNAME);
	}

	private String extractCallId(SOAPHeader header) {
		String callId = null;

		Iterator<Node> headersIter = header.getChildElements(CALLID_QNAME);
		while (headersIter.hasNext()) {
			Node element = headersIter.next();
			if (CALLID_QNAME.getNamespaceURI().equals(element.getNamespaceURI()) &&
					CALLID_QNAME.getLocalPart().equals(element.getLocalName())) {
				callId = element.getValue();
				if (logger.isDebugEnabled()) {
					logger.debug("Found callId: " + callId);
				}
				break;
			}
		}

		if (callId == null) {
			callId = UUID.randomUUID().toString();
		}
		return callId;
	}
}
