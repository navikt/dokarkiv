package no.nav.dokarkiv.core.soap;

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
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

/**
 * Inspired by MDCInHandler in modig-log-jaxws. Since MDCInHandler uses
 * SubjectHandler which in turn expects SAML security we can not use it.
 * Instead we use this class that just handles callId.
 * 
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
public class CallIdHandler implements SOAPHandler<SOAPMessageContext> {

	private static final Logger logger = LoggerFactory.getLogger(CallIdHandler.class);
	
	/** The callID MDC key */
	public static final String MDC_CALL_ID = "callId";

    // QName for the callId header
    private static final QName CALLID_QNAME = new QName("uri:no.nav.applikasjonsrammeverk", MDC_CALL_ID);
	
    /** {@inheritDoc} */
    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        Boolean outbound = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        // INBOUND processing
        if (!outbound) {
        	logger.debug("About to extract callId from SOAP message");
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

    /** {@inheritDoc} */
    @Override
    public boolean handleFault(SOAPMessageContext context) {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void close(MessageContext context) {
    }

    /** {@inheritDoc} */
    @Override
    public Set<QName> getHeaders() {
    	return Collections.singleton(CALLID_QNAME);
    }

    @SuppressWarnings("unchecked")
	private String extractCallId(SOAPHeader header) {
        String callId = "";
        if (header == null) {
            return callId;
        }
        Iterator<SOAPElement> headersIter = header.getChildElements(CALLID_QNAME);
        while (headersIter.hasNext()) {
            SOAPElement element = headersIter.next();
            if (element.getElementQName().equals(CALLID_QNAME)) {
                callId = element.getValue();
                logger.debug("Found callId: " + callId);
                break;
            }
        }
        return callId;
    }

}
