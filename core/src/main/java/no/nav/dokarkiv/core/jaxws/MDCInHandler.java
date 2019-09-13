package no.nav.dokarkiv.core.jaxws;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.modig.core.context.SubjectHandler;
import org.slf4j.MDC;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.xml.namespace.QName;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

/**
 * A patched version of no.nav.modig.jaxws.handlers.MDCInHandler which generates a callId if
 * missing.
 */
@Slf4j
public class MDCInHandler implements SOAPHandler<SOAPMessageContext> {
	// QName for the callId header
	private static final QName CALLID_QNAME = new QName("uri:no.nav.applikasjonsrammeverk", MDCConstants.MDC_CALL_ID);
	
	@Override
	public boolean handleMessage(SOAPMessageContext context) {
		Boolean outbound = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		// INBOUND processing
		if (!outbound) {
			if(log.isDebugEnabled()) {
				log.debug("About to extract callId from SOAP message");
			}
			SOAPHeader header;
			try {
				header = context.getMessage().getSOAPHeader();
			} catch (SOAPException e) {
				log.error(e.getMessage());
				throw new ProtocolException(e);
			}
			String callId = extractOrGenerateNewCallId(header);
			
			SubjectHandler subjectHandler = SubjectHandler.getSubjectHandler();
			
			String userId = validateAndGetUid(subjectHandler.getUid());
			String consumerId = subjectHandler.getConsumerId() == null ? "" : subjectHandler.getConsumerId();

			MDC.put(MDCConstants.MDC_CALL_ID, callId);
			MDC.put(MDCConstants.MDC_USER_ID, userId);
			MDC.put(MDCConstants.MDC_CONSUMER_ID, consumerId);
			if(log.isDebugEnabled()) {
				log.debug(String.format("Values added to MDC. callId=%s, userId=%s, consumerId=%s", callId, userId, consumerId));
			}
		}
		return true;
	}
	
	private String validateAndGetUid(String uid) {
		if(uid != null) {
			if (isRegexMatch(uid)) {
				return uid;
			} else {
				try {
					LdapName name = new LdapName(uid);
					
					for(Rdn rdn : name.getRdns()) {
						if("CN".equalsIgnoreCase(rdn.getType())) {
							return rdn.getValue().toString();
						}
					}
				} catch (InvalidNameException e) {
					return uid;
				}
			}
		}
		return "";
	}
	
	private boolean isRegexMatch(String uid) {
		String uIdRegex = "^[A-Za-z][0-9]{6}$";
		return uid.matches(uIdRegex);
	}
	
	@Override
	public boolean handleFault(SOAPMessageContext context) {
		return true;
	}
	
	@Override
	public void close(MessageContext context) {
		MDC.remove(MDCConstants.MDC_CALL_ID);
		MDC.remove(MDCConstants.MDC_USER_ID);
		MDC.remove(MDCConstants.MDC_CONSUMER_ID);
		if(log.isDebugEnabled()) {
			log.debug("Cleared MDC session");
		}
	}
	
	@Override
	public Set<QName> getHeaders() {
		if(log.isDebugEnabled()) {
			log.debug("CallIdHandler - getHeaders ");
		}

		Set<QName> qNames = new HashSet<>();
		qNames.add(CALLID_QNAME);
		
		return qNames;
	}
	
	@SuppressWarnings("unchecked")
	private String extractOrGenerateNewCallId(SOAPHeader header) {
		String callId = UUID.randomUUID().toString();
		
		if (header == null) {
			return callId;
		}

		Iterator<Node> headersIter = header.getChildElements(CALLID_QNAME);
		while (headersIter.hasNext()) {
			Node element = headersIter.next();
			if (CALLID_QNAME.getNamespaceURI().equals(element.getNamespaceURI()) &&
					CALLID_QNAME.getLocalPart().equals(element.getLocalName())) {
				callId = element.getValue();
				if (log.isDebugEnabled()) {
					log.debug("Found callId: " + callId);
				}
				break;
			}
		}
		return callId;
	}
}
