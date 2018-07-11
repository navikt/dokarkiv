package no.nav.dokarkiv.core.security;

import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.security.SecurityContext;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.wss4j.common.crypto.Crypto;
import org.apache.wss4j.common.crypto.CryptoFactory;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.wss4j.common.principal.SAMLTokenPrincipal;
import org.apache.wss4j.dom.handler.RequestData;
import org.apache.wss4j.dom.handler.WSHandlerConstants;

import java.util.Map;
import java.util.Properties;

/**
 * CXF Soap interceptor som kun validerer SAML-token (logger ikke caller inn i containeren).
 */
@Slf4j
public class ValidateSamlInInterceptor extends WSS4JInInterceptor {
	public ValidateSamlInInterceptor() {
		super();
		setProperty(WSHandlerConstants.ACTION, WSHandlerConstants.SAML_TOKEN_SIGNED);
	}

	public ValidateSamlInInterceptor(boolean ignore) {
		super(ignore);
		setProperty(WSHandlerConstants.ACTION, WSHandlerConstants.SAML_TOKEN_SIGNED);
	}

	public ValidateSamlInInterceptor(Map<String, Object> properties) {
		super(properties);
		setProperty(WSHandlerConstants.ACTION, WSHandlerConstants.SAML_TOKEN_SIGNED);
	}

	@Override
	public Crypto loadSignatureCrypto(RequestData requestData) throws WSSecurityException {

		Properties signatureProperties = new Properties();
		signatureProperties.setProperty("org.apache.wss4j.crypto.merlin.truststore.file", System.getProperty("javax.net.ssl.trustStore"));
		signatureProperties.setProperty("org.apache.wss4j.crypto.merlin.truststore.password", System.getProperty("javax.net.ssl.trustStorePassword"));

		return CryptoFactory.getInstance(signatureProperties);
	}

	@Override
	public void handleMessage(SoapMessage msg) {
		super.handleMessage(msg);

		SecurityContext sc = (SecurityContext) msg.get(SecurityContext.class.getName());
		if(sc == null) {
			throw new RuntimeException("Cannot get SecurityContext from SoapMessage");
		}
		SAMLTokenPrincipal samlTokenPrincipal = (SAMLTokenPrincipal) sc.getUserPrincipal();
		if(samlTokenPrincipal == null) {
			throw new RuntimeException("Cannot get SAMLTokenPrincipal from SecurityContext");
		}
	}

}