package no.nav.dokarkiv.core.jaxws;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.MDC;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

/**
 * Unit tests for CallIdHandler.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
@RunWith(MockitoJUnitRunner.class)
public class CallIdHandlerTest {

	private QName CALLID_QNAME = new QName("uri:no.nav.applikasjonsrammeverk", "callId");
	private String callId = "1999_123";

	private CallIdHandler handler;

	@Mock
	private SOAPMessageContext context;

	private SOAPMessage soapMessage;

	@Before
	public void setUp() throws Exception {
		handler = new CallIdHandler();
		soapMessage = createEmptySoapMessage();

		when(context.getMessage()).thenReturn(soapMessage);
		when(context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)).thenReturn(false);

		MDC.put(CallIdHandler.MDC_CALL_ID, null);
	}

	@Test
	public void shouldSetCallIdInMDC() throws Exception {
		addCallIdHeaderToSOAPMessage();

		handler.handleMessage(context);

		assertThat(MDC.get(CallIdHandler.MDC_CALL_ID), is(callId));
	}

	private void addCallIdHeaderToSOAPMessage() throws Exception {
		SOAPEnvelope envelope = soapMessage.getSOAPPart().getEnvelope();
		SOAPHeader header = envelope.getHeader();

		SOAPElement callIdElement = header.addChildElement(CALLID_QNAME);
		callIdElement.setValue(callId);
	}

	public static SOAPMessage createEmptySoapMessage() throws Exception {
		MessageFactory factory = MessageFactory.newInstance();
		SOAPMessage message = factory.createMessage();
		return message;
	}

}