package no.nav.dokarkiv.core.jaxws;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CALL_ID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

/**
 * Unit tests for CallIdHandler.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
@ExtendWith(MockitoExtension.class)
public class CallIdHandlerTest {

	private QName CALLID_QNAME = new QName("uri:no.nav.applikasjonsrammeverk", "callId");
	private String callId = "1999_123";

	private CallIdHandler handler;

	@Mock
	private SOAPMessageContext context;

	private SOAPMessage soapMessage;

	@BeforeEach
	public void setUp() throws SOAPException {
		handler = new CallIdHandler();
		soapMessage = createEmptySoapMessage();

		when(context.getMessage()).thenReturn(soapMessage);
		when(context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)).thenReturn(false);

		MDC.put(MDC_CALL_ID, null);
	}

	@Test
	public void shouldSetCallIdInMDC() throws SOAPException {
		addCallIdHeaderToSOAPMessage();

		handler.handleMessage(context);

		assertThat(MDC.get(MDC_CALL_ID), is(callId));
	}

	private void addCallIdHeaderToSOAPMessage() throws SOAPException {
		SOAPEnvelope envelope = soapMessage.getSOAPPart().getEnvelope();
		SOAPHeader header = envelope.getHeader();

		SOAPElement callIdElement = header.addChildElement(CALLID_QNAME);
		callIdElement.setValue(callId);
	}

	public static SOAPMessage createEmptySoapMessage() throws SOAPException {
		MessageFactory factory = MessageFactory.newInstance();
		SOAPMessage message = factory.createMessage();
		return message;
	}

}