package no.nav.dokarkiv.core.jaxws;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPEnvelope;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPHeader;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import javax.xml.namespace.QName;

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